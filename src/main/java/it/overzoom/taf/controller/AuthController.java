package it.overzoom.taf.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.overzoom.taf.dto.UserDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.UserMapper;
import it.overzoom.taf.model.Municipal;
import it.overzoom.taf.model.User;
import it.overzoom.taf.repository.MunicipalRepository;
import it.overzoom.taf.repository.UserRepository;
import it.overzoom.taf.utils.SecurityUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final CognitoIdentityProviderClient cognito;
    private final UserRepository userRepository;
    private final MunicipalRepository municipalRepository;
    private final UserMapper userMapper;
    private final String clientId;
    private final String clientSecret;
    private final String userPoolId;

    public AuthController(CognitoIdentityProviderClient cognito,
            UserRepository userRepository,
            UserMapper userMapper,
            MunicipalRepository municipalRepository,
            @Value("${COGNITO_CLIENT_ID}") String clientId,
            @Value("${COGNITO_CLIENT_SECRET}") String clientSecret,
            @Value("${COGNITO_USER_POOL_ID}") String userPoolId) {
        this.cognito = cognito;
        this.userRepository = userRepository;
        this.municipalRepository = municipalRepository;
        this.userMapper = userMapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.userPoolId = userPoolId;
    }

    public static class LoginRequest {
        public String usernameOrEmail;
        public String password;
    }

    public static class RegisterRequest {
        public String name;
        public String surname;
        public String email;
        public String password;
        public String confirmPassword;
    }

    public static class ConfirmRequest {
        public String email;
        public String confirmationCode;
    }

    public static class RefreshTokenRequest {
        public String refreshToken;
        public String userId;
    }

    public static class LogoutRequest {
        public String userId;
    }

    @PostMapping("/login")
    @Operation(summary = "Login utente", description = "Effettua il login dell'utente utilizzando nome utente e password", parameters = {
            @Parameter(name = "usernameOrEmail", description = "Nome utente o email dell'utente", required = true),
            @Parameter(name = "password", description = "Password dell'utente", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Login avvenuto con successo"),
            @ApiResponse(responseCode = "400", description = "Credenziali errate o mancanti"),
            @ApiResponse(responseCode = "500", description = "Errore del server durante il login")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String secretHash = calculateSecretHash(
                req.usernameOrEmail, clientId, clientSecret);
        log.info("Logging in user: {}", req.usernameOrEmail);
        InitiateAuthRequest authReq = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(Map.of(
                        "USERNAME", req.usernameOrEmail,
                        "PASSWORD", req.password,
                        "SECRET_HASH", secretHash))
                .build();

        InitiateAuthResponse resp = cognito.initiateAuth(authReq);
        AuthenticationResultType tok = resp.authenticationResult();
        return ResponseEntity.ok(Map.of(
                "access_token", tok.accessToken(),
                "id_token", tok.idToken(),
                "refresh_token", tok.refreshToken(),
                "expires_in", tok.expiresIn()));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrazione utente", description = "Crea un nuovo account utente nel sistema", parameters = {
            @Parameter(name = "name", description = "Nome dell'utente", required = true),
            @Parameter(name = "surname", description = "Cognome dell'utente", required = true),
            @Parameter(name = "email", description = "Email dell'utente", required = true),
            @Parameter(name = "password", description = "Password dell'utente", required = true),
            @Parameter(name = "confirmPassword", description = "Conferma password dell'utente", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Registrazione completata con successo"),
            @ApiResponse(responseCode = "400", description = "Le password non corrispondono"),
            @ApiResponse(responseCode = "409", description = "L'utente esiste già"),
            @ApiResponse(responseCode = "500", description = "Errore durante la registrazione")
    })
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        log.info("Registering user: {}", req.email);
        if (!req.password.equals(req.confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password and confirmPassword do not match"));
        }

        String secretHash = calculateSecretHash(req.email, clientId, clientSecret);

        try {
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(req.email)
                    .password(req.password)
                    .secretHash(secretHash)
                    .userAttributes(
                            AttributeType.builder().name("given_name").value(req.name).build(),
                            AttributeType.builder().name("family_name").value(req.surname).build(),
                            AttributeType.builder().name("email").value(req.email).build())
                    .build();

            SignUpResponse signUpResponse = cognito.signUp(signUpRequest);

            // UUID Cognito (userSub)
            String userSub = signUpResponse.userSub();

            cognito.adminAddUserToGroup(builder -> builder
                    .userPoolId(userPoolId)
                    .username(req.email)
                    .groupName("ROLE_USER")
                    .build());

            User user = new User();
            user.setUserId(userSub);
            user.setName(req.name);
            user.setSurname(req.surname);
            user.setRoles(new String[] { "ROLE_USER" });

            Municipal municipal = municipalRepository.findByCityAndProvince("Trani", "BT")
                    .orElseThrow(() -> new ResourceNotFoundException("Default municipality not found"));

            user.setMunicipalityIds(new String[] { municipal.getId() });

            User responseFromRegistry = userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "userConfirmed", signUpResponse.userConfirmed(),
                    "userSub", userSub,
                    "userId", responseFromRegistry.getId()));
        } catch (UsernameExistsException e) {
            return ResponseEntity.status(409).body(Map.of("error", "User already exists"));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid password: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    @Operation(summary = "Conferma l'account dell'utente", description = "Conferma un account utente tramite il codice di conferma inviato", parameters = {
            @Parameter(name = "email", description = "Email dell'utente da confermare", required = true),
            @Parameter(name = "confirmationCode", description = "Codice di conferma ricevuto via email", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Account confermato con successo"),
            @ApiResponse(responseCode = "400", description = "Errore nel codice di conferma"),
            @ApiResponse(responseCode = "500", description = "Errore del server durante la conferma")
    })
    public ResponseEntity<?> confirm(@RequestBody ConfirmRequest req) {
        String secretHash = calculateSecretHash(req.email, clientId, clientSecret);

        try {
            cognito.confirmSignUp(builder -> builder
                    .clientId(clientId)
                    .username(req.email)
                    .confirmationCode(req.confirmationCode)
                    .secretHash(secretHash));

            return ResponseEntity.ok(Map.of("status", "Account confirmed successfully"));
        } catch (Exception e) {
            log.error("Failed to confirm sign up", e);
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Recupera il profilo dell'utente", description = "Restituisce le informazioni del profilo dell'utente corrente", responses = {
            @ApiResponse(responseCode = "200", description = "Profilo utente restituito con successo"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    public ResponseEntity<UserDTO> getMyProfile() throws ResourceNotFoundException {
        return userRepository.findByUserId(SecurityUtils.getCurrentUserId()).map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato."));
    }

    private static String calculateSecretHash(String userName,
            String clientId,
            String clientSecret) {
        try {
            String HMAC_SHA256_ALGORITHM = "HmacSHA256";
            SecretKeySpec signingKey = new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            String data = userName + clientId;
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating secret hash", e);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Refreshing token for user: {}, refreshToken: {}", request.userId, request.refreshToken);
        try {
            String secretHash = calculateSecretHash(request.userId, clientId, clientSecret);
            // Use the userId to find the user in the system if needed
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .clientId(clientId)
                    .authParameters(Map.of(
                            "USERNAME", request.userId,
                            "REFRESH_TOKEN", request.refreshToken,
                            "SECRET_HASH", secretHash))
                    .build();

            InitiateAuthResponse response = cognito.initiateAuth(authRequest);
            AuthenticationResultType result = response.authenticationResult();

            return ResponseEntity.ok(Map.of(
                    "access_token", result.accessToken(),
                    "id_token", result.idToken(),
                    "expires_in", result.expiresIn(),
                    "token_type", result.tokenType(),
                    "refresh_token", request.refreshToken));
        } catch (Exception e) {
            log.error("Error while refreshing token", e);
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token invalid or expired"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        log.info("Logout user: {}", request.userId);
        try {
            cognito.adminUserGlobalSignOut(builder -> builder
                    .userPoolId(userPoolId)
                    .username(request.userId)
                    .build());
            // Remove FCM token from the user
            userRepository.findByUserId(request.userId).ifPresent(user -> {
                log.info("Removing FCM token for user: {}", user.getId());
                user.setFcmToken(null); // Remove the FCM token
                userRepository.save(user); // Save the changes
            });

            return ResponseEntity.ok(Map.of("message", "Logout effettuato con successo"));
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(500).body(Map.of("error", "Logout failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount() {
        String userId;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (ResourceNotFoundException e) {
            log.error("User not found during account deletion", e);
            return ResponseEntity.status(404).body(Map.of("error", "Utente non trovato."));
        }
        log.info("Deleting account for user: {}", userId);
        try {
            userRepository.findByUserId(userId).ifPresent(user -> {
                userRepository.delete(user);
            });
            cognito.adminDeleteUser(builder -> builder
                    .userPoolId(userPoolId)
                    .username(userId)
                    .build());
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            log.error("Error during account deletion", e);
            return ResponseEntity.status(500).body(Map.of("error", "Account deletion failed: " + e.getMessage()));
        }
    }

}

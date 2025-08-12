package it.overzoom.taf.utils;

import java.util.Collection;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import it.overzoom.taf.exception.ResourceNotFoundException;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    private static Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private static boolean isAnonOrInvalid(Authentication a) {
        return a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken;
    }

    private static Optional<Jwt> currentJwtOpt() {
        Authentication a = auth();
        if (isAnonOrInvalid(a))
            return Optional.empty();

        if (a instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.ofNullable(jwtAuth.getToken());
        }
        Object principal = a.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    /** ID utente (tipicamente claim "sub") come Optional, non lancia in anonimo */
    public static Optional<String> getCurrentUserIdOpt() {
        return currentJwtOpt().map(jwt -> jwt.getClaimAsString("sub"));
    }

    /** ID utente obbligatorio, lancia se non autenticato */
    public static String getCurrentUserId() throws ResourceNotFoundException {
        return getCurrentUserIdOpt()
                .orElseThrow(() -> new ResourceNotFoundException("Utente non autenticato."));
    }

    public static Optional<String> getUsernameOpt() {
        // adatta se il tuo “username” è in un altro claim
        return currentJwtOpt().map(jwt -> jwt.getClaimAsString("sub"));
    }

    public static String getUsername() throws ResourceNotFoundException {
        return getUsernameOpt()
                .orElseThrow(() -> new ResourceNotFoundException("Utente non autenticato."));
    }

    public static boolean isCurrentUser(String userId) {
        return getCurrentUserIdOpt().map(id -> id.equals(userId)).orElse(false);
    }

    public static boolean isAdmin() {
        Authentication a = auth();
        if (isAnonOrInvalid(a))
            return false;

        // 1) Controllo sulle authorities (se hai configurato un converter che mappa i
        // gruppi/ruoli)
        Collection<? extends GrantedAuthority> auths = a.getAuthorities();
        for (GrantedAuthority ga : auths) {
            String role = ga.getAuthority();
            if ("ROLE_ADMIN".equals(role) || "ADMIN".equals(role)) {
                return true;
            }
        }

        // 2) Controllo diretto sul claim Cognito "cognito:groups"
        return currentJwtOpt()
                .map(jwt -> jwt.getClaimAsStringList("cognito:groups"))
                .map(groups -> groups.contains("ADMIN") || groups.contains("ROLE_ADMIN"))
                .orElse(false);
    }
}

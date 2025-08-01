package it.overzoom.taf.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.User;
import it.overzoom.taf.service.UserService;
import it.overzoom.taf.utils.SecurityUtils;

@RestController
@RequestMapping("/api/fcm")
public class FCMController {

    private final UserService userService;

    public FCMController(UserService userService) {
        this.userService = userService;
    }

    private static class RegisterTokenRequest {
        private String token;

        public String getToken() {
            return token;
        }
    }

    @PostMapping("/register-token")
    public ResponseEntity<?> registerFcmToken(@RequestBody RegisterTokenRequest req, Authentication auth)
            throws ResourceNotFoundException {
        User user = userService.findByUserId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        String newToken = req.getToken();

        user.setFcmToken(newToken);
        userService.update(user);

        return ResponseEntity.ok().build();
    }
}

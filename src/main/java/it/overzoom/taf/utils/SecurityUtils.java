package it.overzoom.taf.utils;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import it.overzoom.taf.exception.ResourceNotFoundException;

public class SecurityUtils {

    public static String getCurrentUserId() throws ResourceNotFoundException {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .map(jwt -> (String) jwt.getClaim("sub"))
                .orElseThrow(() -> new ResourceNotFoundException("Utente non autenticato."));
    }

    public static String getUsername() throws ResourceNotFoundException {
        Jwt jwt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non autenticato."));

        return jwt.getClaim("sub");
    }

    public static boolean isCurrentUser(String userId) throws ResourceNotFoundException {
        String currentUserId = getCurrentUserId();
        return currentUserId.equals(userId);
    }

    public static boolean isAdmin() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<String> groups = jwt.getClaim("cognito:groups");

        return groups != null && groups.contains("ROLE_ADMIN");
    }

}

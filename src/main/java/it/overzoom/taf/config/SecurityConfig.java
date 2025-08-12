package it.overzoom.taf.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors
                                                .configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers(
                                                                "/api/auth/login", "/api/auth/register",
                                                                "/api/auth/confirm", "/api/auth/refresh-token",
                                                                "/api/events/search", "/api/events/types",
                                                                "/api/news/search",
                                                                "/api/activities/search", "/api/activities/types",
                                                                "/api/activities/tags",
                                                                "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**",
                                                                "/api/public/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/activities/*").permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .anyRequest().authenticated())

                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwkSetUri(
                                                                "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_bSXSXHEow/.well-known/jwks.json")))
                                .logout(logout -> logout.logoutSuccessHandler(new CognitoLogoutHandler()));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(
                                "http://localhost:4200", // Development environment
                                "https://trani.autismfriendly.overzoom.it" // Production environment
                ));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}

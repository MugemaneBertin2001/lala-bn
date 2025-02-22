package lala.house.lala.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import lala.house.lala.services.AuthService;
import lala.house.lala.entities.User;
import lala.house.lala.enums.UserRole;
import org.springframework.security.oauth2.core.user.OAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import lala.house.lala.dto.AuthResponse;
import lala.house.lala.dto.CompleteRegistrationRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final ObjectMapper objectMapper;

    @Value("${frontend.url}")
    private String frontendUrl;

    public AuthController(AuthService authService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.authService = authService;
    }

    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        try {
            User user = authService.getCurrentUser(principal);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/google/login")
    public void initiateGoogleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/success")
    public ResponseEntity<Void> handleLoginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        try {
            AuthResponse response = authService.handleGoogleLogin(principal);

            String jsonResponse = objectMapper.writeValueAsString(response);
            String encodedData = URLEncoder.encode(jsonResponse, StandardCharsets.UTF_8);

            // Redirect to frontend with success data
            String redirectUrl = frontendUrl + "/auth?data=" + encodedData;
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        } catch (Exception e) {
            log.error("Error handling Google login success", e);

            // Encode error message
            String errorMessage = URLEncoder.encode("Login failed. Please try again.", StandardCharsets.UTF_8);
            String redirectUrl = frontendUrl + "/auth?error=" + errorMessage;

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }
    }

    @PutMapping("/complete-registration")
    public ResponseEntity<AuthResponse> completeRegistration(@RequestBody CompleteRegistrationRequest request) {
        try {
            AuthResponse response = authService.completeRegistration(request.getEmail(), request.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error completing registration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/role")
    public ResponseEntity<User> updateUserRole(@AuthenticationPrincipal OAuth2User principal,
            @RequestParam UserRole role) {
        try {
            String email = principal.getAttribute("email");
            User updatedUser = authService.updateUserRole(email, role);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error updating user role", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal OAuth2User principal) {
        try {
            String email = principal.getAttribute("email");
            User user = authService.getUserProfile(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error getting user profile", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
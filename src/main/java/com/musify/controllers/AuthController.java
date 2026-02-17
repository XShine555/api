package com.musify.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musify.DTOs.Auth.AuthResponseDTO;
import com.musify.DTOs.Auth.LoginRequestDTO;
import com.musify.DTOs.Auth.RegisterRequestDTO;
import com.musify.models.User;
import com.musify.services.AuthService;
import com.musify.services.JwtService;
import com.musify.services.UserService;

import io.micrometer.common.util.StringUtils;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    public AuthController(UserService userService, AuthService authService, JwtService jwtService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/signIn")
    public ResponseEntity<AuthResponseDTO> signIn(@RequestBody RegisterRequestDTO request) {
        if (userService.getUserByUsername(request.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signIn(request));
    }

    @PostMapping("/signUp")
    public ResponseEntity<AuthResponseDTO> signUp(@RequestBody LoginRequestDTO request) {
        if (userService.getUserByUsername(request.username()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(authService.signUp(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        User user = (User)authentication.getPrincipal();

        Map<String, String> userInfo = Map.of(
            "username", user.getUsername(),
            "imagePath", StringUtils.isNotBlank(user.getImagePath()) ? user.getImagePath() : ""
        );

        return ResponseEntity.ok(userInfo);
    }
}

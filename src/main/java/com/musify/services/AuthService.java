package com.musify.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.musify.DTOs.Auth.AuthResponseDTO;
import com.musify.DTOs.Auth.LoginRequestDTO;
import com.musify.DTOs.Auth.RegisterRequestDTO;
import com.musify.DTOs.User.UserCreateDTO;
import com.musify.models.User;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserService userService, JwtService jwtService,
            PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDTO signIn(RegisterRequestDTO request) {
        logger.info("Registering user: {}", request.username());
        User user = userService.createUser(
            new UserCreateDTO(request.username(), request.password())
        );
        return new AuthResponseDTO(jwtService.generateToken(user));
    }

    public AuthResponseDTO signUp(LoginRequestDTO request) {
        logger.info("Login attempt for user: {}", request.username());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userService.getUserByUsername(request.username()).orElseThrow();
        return new AuthResponseDTO(jwtService.generateToken(user));
    }
}

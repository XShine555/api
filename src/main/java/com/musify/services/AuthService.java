package com.musify.services;

import java.util.Date;
import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.musify.DTOs.Auth.AuthResponseDTO;
import com.musify.DTOs.Auth.LoginRequestDTO;
import com.musify.DTOs.Auth.RegisterRequestDTO;
import com.musify.DTOs.User.UserCreateDTO;
import com.musify.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    public AuthService(UserService userService, JwtService jwtService,
            PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDTO signIn(RegisterRequestDTO request) {
        logger.info("Registering user: {}", request.username());
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userService.createUser(
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

    public String generateToken(User user) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("imagePath", StringUtils.isBlank(user.getImagePath()) ? user.getImagePath() : "")
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public String extractImagePath(String token) {
        return extractAllClaims(token).get("imagePath", String.class);
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}

package com.musify.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.musify.DTOs.User.UserCreateDTO;
import com.musify.DTOs.User.UserUpdateDTO;
import com.musify.models.User;
import com.musify.repositories.UserRepository;

import io.micrometer.common.util.StringUtils;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USER_IMAGE_DIR = "private/images/users/";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User createUser(UserCreateDTO dto) {
        logger.info("Creating user: {}", dto.username());
        User user = new User();
        user.setUsername(dto.username());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        logger.debug("Retrieving all users");
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        logger.debug("Retrieving user with ID: {}", id);
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        logger.debug("Retrieving user with username: {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> updateUser(Long id, UserUpdateDTO dto) {
        logger.info("Updating user with ID: {}", id);
        return userRepository.findById(id).map(user -> {
            if (StringUtils.isNotBlank(dto.username())) {
                user.setUsername(dto.username());
            }
            if (StringUtils.isNotBlank(dto.password())) {
                user.setPasswordHash(passwordEncoder.encode(dto.password()));
            }
            return userRepository.save(user);
        });
    }

    public boolean deleteUserById(Long id) {
        logger.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("User not found with ID: {}", id);
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    public void deleteAllUsers() {
        logger.warn("Deleting all users");
        userRepository.deleteAll();
    }

    public boolean updateImagePath(Long userId, MultipartFile file) throws IOException {
        logger.info("Updating image for user ID: {}", userId);
        return userRepository.findById(userId).map(user -> {
            try {
                Path uploadDir = Paths.get(USER_IMAGE_DIR);
                Files.createDirectories(uploadDir);
                String filename = "user_" + user.getId() + "_" + file.getOriginalFilename();
                Path targetPath = uploadDir.resolve(filename);
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                user.setImagePath(targetPath.toString());
                userRepository.save(user);
                return true;
            } catch (IOException e) {
                logger.error("Error saving image for User ID: {}", userId, e);
                throw new RuntimeException("Failed to save image", e);
            }
        }).orElseGet(() -> {
            logger.warn("User not found with ID: {}", userId);
            return false;
        });
    }
}
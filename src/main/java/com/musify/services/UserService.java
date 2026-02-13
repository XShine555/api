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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.musify.DTOs.UserController.UserCreateDTO;
import com.musify.DTOs.UserController.UserResponseDTO;
import com.musify.DTOs.UserController.UserUpdateDTO;
import com.musify.models.User;
import com.musify.repositories.UserRepository;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USER_IMAGE_DIR = "private/images/users/";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getImagePath(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private User toEntity(UserCreateDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(dto.getPassword());
        return user;
    }

    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        logger.info("Creating user: {}", userCreateDTO.getUsername());
        User createdUser = userRepository.save(toEntity(userCreateDTO));
        return toResponseDTO(createdUser);
    }

    public List<UserResponseDTO> getAllUsers() {
        logger.debug("Retrieving all users");
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public Optional<UserResponseDTO> getUserById(Long id) {
        logger.debug("Retrieving user with ID: {}", id);
        return userRepository.findById(id)
                .map(this::toResponseDTO);
    }

    public Optional<UserResponseDTO> getUserByUsername(String username) {
        logger.debug("Retrieving user with username: {}", username);
        return userRepository.findByUsername(username)
                .map(this::toResponseDTO);
    }

    public Optional<UserResponseDTO> updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        logger.info("Updating user with ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userUpdateDTO.getUsername());
                    user.setPasswordHash(userUpdateDTO.getPassword());
                    return userRepository.save(user);
                })
                .map(this::toResponseDTO);
    }

    public boolean deleteUserById(Long id) {
        logger.info("Deleting user with ID: {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        logger.warn("User not found with ID: {}", id);
        return false;
    }

    public void deleteAllUsers() {
        logger.warn("Deleting all users");
        userRepository.deleteAll();
    }

    public boolean updateImagePath(Long userId, MultipartFile multipartFile) throws IOException {
        logger.info("Updating image for user ID: {}", userId);

        return userRepository.findById(userId)
                .map(user -> {
                    try {
                        Path uploadDir = Paths.get(USER_IMAGE_DIR);
                        Files.createDirectories(uploadDir);

                        String filename = "user_" + user.getId() + "_" + multipartFile.getOriginalFilename();
                        Path targetPath = uploadDir.resolve(filename);

                        Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        user.setImagePath(targetPath.toString());
                        userRepository.save(user);
                        return true;
                    } catch (IOException e) {
                        logger.error("Error saving image for User ID: {}", userId, e);
                        throw new RuntimeException("Failed to save image", e);
                    }
                })
                .orElseGet(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return false;
                });
    }
}

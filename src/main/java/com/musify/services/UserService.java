package com.musify.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.musify.DTOs.UserController.UserCreateDTO;
import com.musify.DTOs.UserController.UserResponseDTO;
import com.musify.DTOs.UserController.UserUpdateDTO;
import com.musify.logging.CustomLogging;
import com.musify.models.User;
import com.musify.repositories.UserRepository;

@Service
public class UserService {
    private static final String USER_IMAGE_DIR = "private/images/users/";

    @Autowired
    UserRepository userRepository;
    @Autowired
    CustomLogging logger;

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
        user.setPasswordHash(dto.getPasswordHash());
        return user;
    }

    private User toEntity(UserUpdateDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(dto.getPasswordHash());
        return user;
    }

    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        logger.info(getClass().getSimpleName(), "createUser", "Creating user: " + userCreateDTO.getUsername());
        User user = toEntity(userCreateDTO);
        userRepository.create(user);
        User createdUser = userRepository.findByUsername(user.getUsername()).orElseThrow();
        logger.info(getClass().getSimpleName(), "createUser", "User created: " + userCreateDTO.getUsername());
        return toResponseDTO(createdUser);
    }

    public List<UserResponseDTO> getAllUsers() {
        logger.info(getClass().getSimpleName(), "getAllUsers", "Retrieving all users");
        List<User> users = userRepository.findAll();
        logger.info(getClass().getSimpleName(), "getAllUsers", "Retrieved " + users.size() + " users");
        return users.stream().map(this::toResponseDTO).toList();
    }

    public Optional<UserResponseDTO> getUserById(Long id) {
        logger.info(getClass().getSimpleName(), "getUserById", "Retrieving user with ID: " + id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.info(getClass().getSimpleName(), "getUserById", "User found with ID: " + id);
            return Optional.of(toResponseDTO(user.get()));
        } else {
            logger.warn(getClass().getSimpleName(), "getUserById", "User not found with ID: " + id);
            return Optional.empty();
        }
    }

    public Optional<UserResponseDTO> getUserByUsername(String username) {
        logger.info(getClass().getSimpleName(), "getUserByUsername", "Retrieving user with username: " + username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            logger.info(getClass().getSimpleName(), "getUserByUsername", "User found with username: " + username);
            return Optional.of(toResponseDTO(user.get()));
        } else {
            logger.warn(getClass().getSimpleName(), "getUserByUsername", "User not found with username: " + username);
            return Optional.empty();
        }
    }

    public Optional<UserResponseDTO> updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        logger.info(getClass().getSimpleName(), "updateUser", "Updating user with ID: " + id);
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = toEntity(userUpdateDTO);
            user.setId(id);
            userRepository.update(user);
            User updatedUser = userRepository.findById(id).orElseThrow();
            logger.info(getClass().getSimpleName(), "updateUser", "User updated with ID: " + id);
            return Optional.of(toResponseDTO(updatedUser));
        }
        logger.warn(getClass().getSimpleName(), "updateUser", "User not found with ID: " + id);
        return Optional.empty();
    }

    public boolean deleteUserById(Long id) {
        logger.info(getClass().getSimpleName(), "deleteUserById", "Deleting user with ID: " + id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
            logger.info(getClass().getSimpleName(), "deleteUserById", "User deleted with ID: " + id);
            return true;
        }
        logger.warn(getClass().getSimpleName(), "deleteUserById", "User not found with ID: " + id);
        return false;
    }

    public void deleteAllUsers() {
        logger.info(getClass().getSimpleName(), "deleteAllUsers", "Deleting all users");
        userRepository.deleteAll();
        logger.info(getClass().getSimpleName(), "deleteAllUsers", "All users deleted");
    }

    public boolean updateImagePath(Long userId, MultipartFile multipartFile) throws IOException {
        logger.info(getClass().getSimpleName(), "updateImagePath", "Updating image for user ID: " + userId);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            logger.warn(getClass().getSimpleName(), "updateImagePath", "User not found with ID: " + userId);
            return false;
        }

        User user = optionalUser.get();
        File upload_dir = new File(USER_IMAGE_DIR);
        if (!upload_dir.exists()) {
            upload_dir.mkdirs();
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String imagePath = USER_IMAGE_DIR + "user_" + user.getId() + "_" + originalFilename;
        File destinationFile = new File(imagePath);

        Files.copy(multipartFile.getInputStream(), destinationFile.toPath());
        user.setImagePath(imagePath);
        userRepository.updateWithImage(user);
        logger.info(getClass().getSimpleName(), "updateImagePath", "Image updated for user ID: " + userId);
        return true;
    }

    public List<UserResponseDTO> uploadUsersFromCsv(MultipartFile csvFile) throws IOException {
        logger.info(getClass().getSimpleName(), "uploadUsersFromCsv", "Starting CSV upload");
        List<UserResponseDTO> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream()))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 2) {
                    String username = fields[0].trim();
                    String passwordHash = fields[1].trim();
                    User user = new User();
                    user.setUsername(sanitize(username));
                    user.setPasswordHash(sanitize(passwordHash));
                    if (userRepository.findByUsername(sanitize(username)).isPresent()) {
                        logger.warn(getClass().getSimpleName(), "uploadUsersFromCsv",
                                "User already exists with username: " + username);
                        continue;
                    }
                    userRepository.create(user);
                    User createdUser = userRepository.findByUsername(username).orElseThrow();
                    users.add(toResponseDTO(createdUser));
                }
            }
        }

        logger.info(getClass().getSimpleName(), "uploadUsersFromCsv",
                "CSV upload completed. Created " + users.size() + " users");
        return users;
    }

    private static String sanitize(String input) {
        if (input == null)
            return null;
        input = input.replaceAll("\\p{C}", "");
        input = input.replaceAll("\\s+", "");
        return input;
    }
}

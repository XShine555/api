package com.musify.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.musify.logging.CustomLogging;
import com.musify.models.User;
import com.musify.repositories.UserRepository;

@Service
public class UserService {
    private static final String USER_IMAGE_DIR = "/tmp/images/users/";
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    CustomLogging logger;

    public Optional<User> getUserById(Long id) {
        logger.info(getClass().getSimpleName(), "getUserById", "Retrieving user with ID: " + id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.info(getClass().getSimpleName(), "getUserById", "User found with ID: " + id);
        } else {
            logger.warn(getClass().getSimpleName(), "getUserById", "User not found with ID: " + id);
        }
        return user;
    }

    public void updateImagePath(User user, MultipartFile multipartFile) throws IOException {
        logger.info(getClass().getSimpleName(), "updateImagePath", "Updating image path for user ID: " + user.getId());
        File upload_dir = new File(USER_IMAGE_DIR);
        if (!upload_dir.exists()) {
            upload_dir.mkdirs();
            logger.info(getClass().getSimpleName(), "updateImagePath", "Created upload directory: " + USER_IMAGE_DIR);
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String imagePath = USER_IMAGE_DIR + "user_" + user.getId() + "_" + originalFilename;
        File destinationFile = new File(imagePath);

        Files.copy(multipartFile.getInputStream(), destinationFile.toPath());
        user.setImagePath(imagePath);
        userRepository.update(user);
        logger.info(getClass().getSimpleName(), "updateImagePath", "Successfully updated image path for user ID: " + user.getId() + " to: " + imagePath);
    }

    public List<User> uploadUsersFromCsv(MultipartFile csvFile) throws IOException {
        logger.info(getClass().getSimpleName(), "uploadUsersFromCsv", "Starting CSV upload for users");
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length >= 3) {
                    String username = fields[0].trim();
                    String passwordHash = fields[1].trim();
                    String imagePath = fields[2].trim();
                    User user = new User();
                    user.setUsername(username);
                    user.setPasswordHash(passwordHash);
                    user.setImagePath(imagePath);
                    userRepository.create(user);
                    users.add(user);
                    logger.info(getClass().getSimpleName(), "uploadUsersFromCsv", "Created user: " + username);
                }
            }
        }
        logger.info(getClass().getSimpleName(), "uploadUsersFromCsv", "CSV upload completed, created " + users.size() + " users");
        return users;
    }
}

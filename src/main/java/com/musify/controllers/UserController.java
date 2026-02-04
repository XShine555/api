package com.musify.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.musify.logging.CustomLogging;
import com.musify.models.User;
import com.musify.services.UserService;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    CustomLogging logger;

    @GetMapping("/api/test")
    public ResponseEntity<String> test() {
        logger.info(getClass().getSimpleName(), "test", "Test endpoint called");
        return ResponseEntity.ok("API is working!");
    }

    @GetMapping("/users/{id}/image")
    public ResponseEntity<String> updateUserImagePath(@PathVariable("id") Long id) {
        logger.info(getClass().getSimpleName(), "updateUserImagePath", "GET Endpoint called with ID: " + id);
        return ResponseEntity.ok("Image endpoint called for user " + id);
    }

    @PostMapping("/users/{id}/image")
    public ResponseEntity<?> updateUserImage(@PathVariable("id") Long id, @RequestParam("profilePicture") MultipartFile profilePicture) {
        logger.info(getClass().getSimpleName(), "updateUserImage", "POST endpoint called for user ID: " + id);
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isEmpty()) {
            logger.error(getClass().getSimpleName(), "updateUserImage", "User with ID: " + id + " not found.");
            return ResponseEntity.notFound().build();
        }

        try {
            userService.updateImagePath(optionalUser.get(), profilePicture);
            logger.info(getClass().getSimpleName(), "updateUserImage", "Successfully updated image path for User ID: " + id);
            return ResponseEntity.ok().build();
        } catch (IOException ioException) {
            logger.error(getClass().getSimpleName(), "updateUserImage", "Failed to update image path for User ID: " + id + ". Error: " + ioException.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error(getClass().getSimpleName(), "updateUserImage", "Unexpected error for User ID: " + id + ". Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/users/upload-csv")
    public ResponseEntity<?> uploadUsersFromCsv(@RequestParam("file") MultipartFile csvFile) {
        if (csvFile.isEmpty()) {
            logger.warn(getClass().getSimpleName(), "uploadUsersFromCsv", "Uploaded CSV file is empty");
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            List<User> users = userService.uploadUsersFromCsv(csvFile);
            logger.info(getClass().getSimpleName(), "uploadUsersFromCsv", "Successfully uploaded " + users.size() + " users from CSV");
            return ResponseEntity.ok("Uploaded " + users.size() + " users");
        } catch (IOException ioException) {
            logger.error(getClass().getSimpleName(), "uploadUsersFromCsv", "Failed to upload users from CSV. Error: " + ioException.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process CSV file");
        }
    }
}

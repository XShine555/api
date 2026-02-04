package com.musify.controllers;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PatchMapping("/users/{id}/image")
    public ResponseEntity<?> updateUserImagePath(Long id,
            @RequestParam("profilePicture") MultipartFile profilePicture) {
        Optional<User> optionalUser = userService.getUserById(id);

        if (optionalUser.isEmpty()) {
            logger.error(getClass().getSimpleName(), "UpdateUserImagePath", "User with ID: " + id + " not found.");
            return ResponseEntity.notFound().build();
        }

        try {
            userService.updateImagePath(optionalUser.get(), profilePicture);
            logger.info(getClass().getSimpleName(), "UpdateUserImagePath",
                    "Successfully updated image path for User ID: " + id);
            return ResponseEntity.ok().build();
        } catch (IOException ioException) {
            logger.error(getClass().getSimpleName(), "UpdateUserImagePath",
                    "Failed to update image path for User ID: " + id + ". Error: " + ioException.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

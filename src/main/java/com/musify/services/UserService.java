package com.musify.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.musify.models.User;
import com.musify.repositories.UserRepository;

@Service
public class UserService {
    private static final String USER_IMAGE_DIR = "/images/users/";
    
    @Autowired
    UserRepository userRepository;

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void updateImagePath(User user, MultipartFile multipartFile) throws IOException {
        File upload_dir = new File(USER_IMAGE_DIR);
        if (!upload_dir.exists()) {
            upload_dir.mkdirs();
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String imagePath = USER_IMAGE_DIR + "user_" + user.getId() + "_" + originalFilename;
        File destinationFile = new File(imagePath);

        Files.copy(multipartFile.getInputStream(), destinationFile.toPath());
        user.setImagePath(imagePath);
        userRepository.update(user);
    }
}

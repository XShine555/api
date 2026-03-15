package com.musify.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.musify.DTOs.Search.SearchResult;
import com.musify.DTOs.Search.SearchResultType;
import com.musify.models.User;
import com.musify.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private SearchResult toSearchResult(User user) {
        String imageUrl = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/images/users/")
            .path(user.getImagePath())
            .toUriString();

        return new SearchResult(
                user.getId(),
                user.getUsername(),
                imageUrl,
                "@" + user.getUsername(),
                SearchResultType.USER);
    }

    @GetMapping
    public ResponseEntity<List<SearchResult>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(this::toSearchResult)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SearchResult> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(this::toSearchResult)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

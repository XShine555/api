package com.musify.DTOs.User;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String username,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

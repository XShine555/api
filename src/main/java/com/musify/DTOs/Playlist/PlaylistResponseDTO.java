package com.musify.DTOs.Playlist;

import java.time.LocalDateTime;

public record PlaylistResponseDTO(
        Long id,
        String title,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

package com.musify.DTOs.Playlist;

import java.time.LocalDateTime;

public record PlaylistResponseDTO(
        Long id,
        String name,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

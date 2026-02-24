package com.musify.DTOs.Playlist;

import java.time.LocalDateTime;

public record PreviewPlaylistResponseDTO(
        Long id,
        String title,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

package com.musify.DTOs.Playlist;

import org.springframework.web.multipart.MultipartFile;

public record PlaylistUpdateDTO(String title, MultipartFile image) {
}

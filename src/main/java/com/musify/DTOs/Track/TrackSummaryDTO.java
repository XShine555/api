package com.musify.DTOs.Track;

import com.musify.DTOs.User.UserSummaryDTO;

public record TrackSummaryDTO(
        Long id,
        String title,
        UserSummaryDTO artist,
        Integer durationSeconds,
        String imageUrl) {
}
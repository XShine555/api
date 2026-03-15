package com.musify.DTOs.Search;

public record SearchResult(
        Long id,
        String title,
        String imageUrl,
        String subtitle,
        SearchResultType type) {
}

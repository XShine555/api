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
import com.musify.services.TrackService;

@RestController
@RequestMapping("/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    private SearchResult toSearchResult(com.musify.models.Track track) {
        String imageBase = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/images/tracks/")
                .toUriString();

        return new SearchResult(
            track.getId(),
                track.getTitle(),
                imageBase + track.getImagePath(),
                track.getArtist().getUsername(),
                SearchResultType.TRACK);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<SearchResult>> getAllTracks() {
        return ResponseEntity.ok(trackService.getAllTracks().stream()
                .map(this::toSearchResult)
                .toList());
    }
}

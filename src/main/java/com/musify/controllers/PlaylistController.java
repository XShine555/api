package com.musify.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.musify.DTOs.Playlist.PlaylistCreateDTO;
import com.musify.DTOs.Playlist.PlaylistResponseDTO;
import com.musify.DTOs.Playlist.PlaylistUpdateDTO;
import com.musify.models.Playlist;
import com.musify.services.PlaylistService;

import io.micrometer.common.util.StringUtils;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    private PlaylistResponseDTO toResponseDTO(Playlist playlist) {
        return new PlaylistResponseDTO(
                playlist.getId(),
                playlist.getName(),
                playlist.getImagePath(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt());
    }

    @PostMapping
    public ResponseEntity<PlaylistResponseDTO> createPlaylist(@RequestBody PlaylistCreateDTO dto) {
        Playlist created = playlistService.createPlaylist(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(created));
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDTO>> getAllPlaylists(
            @RequestParam(value = "name", required = false) String name) {
        List<Playlist> playlists = StringUtils.isNotBlank(name)
                ? playlistService.searchByName(name)
                : playlistService.getAllPlaylists();
        return ResponseEntity.ok(playlists.stream().map(this::toResponseDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> getPlaylistById(@PathVariable Long id) {
        return playlistService.getPlaylistById(id)
                .map(this::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> updatePlaylist(@PathVariable Long id,
            @RequestBody PlaylistUpdateDTO dto) {
        return playlistService.updatePlaylist(id, dto)
                .map(this::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylistById(@PathVariable Long id) {
        return playlistService.deletePlaylistById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

package com.musify.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import com.musify.DTOs.Playlist.PlaylistResponseDTO;
import com.musify.DTOs.Playlist.PlaylistUpdateDTO;
import com.musify.DTOs.Playlist.PreviewPlaylistResponseDTO;
import com.musify.DTOs.Track.TrackSummaryDTO;
import com.musify.DTOs.User.UserSummaryDTO;
import com.musify.exceptions.NotFoundException;
import com.musify.models.Playlist;
import com.musify.services.PlaylistService;
import io.micrometer.common.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {

        private final PlaylistService playlistService;
        private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

        public PlaylistController(PlaylistService playlistService) {
                this.playlistService = playlistService;
        }

        private PreviewPlaylistResponseDTO toPreviewResponseDTO(Playlist playlist) {
                String imageUrl = ServletUriComponentsBuilder
                                .fromCurrentContextPath()
                                .path("/images/playlists/")
                                .path(playlist.getImagePath())
                                .toUriString();

                return new PreviewPlaylistResponseDTO(
                                playlist.getId(),
                                playlist.getTitle(),
                                imageUrl,
                                playlist.getCreatedAt(),
                                playlist.getUpdatedAt());
        }

        private PlaylistResponseDTO toResponseDTO(Playlist playlist) {
                String playlistImageUrl = ServletUriComponentsBuilder
                                .fromCurrentContextPath()
                                .path("/images/playlists/")
                                .path(playlist.getImagePath())
                                .toUriString();

                String trackImageUrl = ServletUriComponentsBuilder
                                .fromCurrentContextPath()
                                .path("/images/tracks/")
                                .toUriString();

                return new PlaylistResponseDTO(
                                playlist.getId(),
                                playlist.getTitle(),
                                playlistImageUrl,
                                playlist.getTracks().stream()
                                                .map(track -> new TrackSummaryDTO(
                                                                track.getId(),
                                                                track.getTitle(),
                                                                new UserSummaryDTO(
                                                                                track.getArtist().getId(),
                                                                                track.getArtist().getUsername()),
                                                                track.getDurationSeconds(),
                                                                String.format("%s%s", trackImageUrl,
                                                                                track.getImagePath())))
                                                .toList(),
                                playlist.getCreatedAt(),
                                playlist.getUpdatedAt());
        }

        @PostMapping
        public ResponseEntity<PreviewPlaylistResponseDTO> createPlaylist(@AuthenticationPrincipal Jwt jwt) {
                Long userId = Long.valueOf(jwt.getSubject());
                Playlist created = playlistService.createPlaylist(userId);
                return ResponseEntity.status(HttpStatus.CREATED).body(toPreviewResponseDTO(created));
        }

        @GetMapping
        public ResponseEntity<List<PreviewPlaylistResponseDTO>> getAllPlaylists(
                        @RequestParam(value = "title", required = false) String title) {
                List<Playlist> playlists = StringUtils.isNotBlank(title)
                                ? playlistService.searchByTitle(title)
                                : playlistService.getAllPlaylists();
                return ResponseEntity.ok(playlists.stream().map(this::toPreviewResponseDTO).toList());
        }

        @GetMapping("/{id}")
        public ResponseEntity<PreviewPlaylistResponseDTO> getPlaylistById(@PathVariable Long id) {
                return playlistService.getPlaylistById(id)
                                .map(this::toPreviewResponseDTO)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/{id}/tracks")
        public ResponseEntity<List<TrackSummaryDTO>> getTracksByPlaylistId(
                        @PathVariable Long id,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction) {
                return playlistService.getFilteredTracksByPlaylistId(
                                id,
                                sortBy,
                                direction)
                                .map(playlistTracks -> playlistTracks.stream()
                                                .map(playlistTrack -> new TrackSummaryDTO(
                                                                playlistTrack.getTrack().getId(),
                                                                playlistTrack.getTrack().getTitle(),
                                                                new UserSummaryDTO(
                                                                                playlistTrack.getTrack().getArtist()
                                                                                                .getId(),
                                                                                playlistTrack.getTrack().getArtist()
                                                                                                .getUsername()),
                                                                playlistTrack.getTrack().getDurationSeconds(),
                                                                ServletUriComponentsBuilder
                                                                                .fromCurrentContextPath()
                                                                                .path("/images/tracks/")
                                                                                .path(playlistTrack.getTrack()
                                                                                                .getImagePath())
                                                                                .toUriString()))
                                                .toList())
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @PutMapping("/{id}")
        public ResponseEntity<PreviewPlaylistResponseDTO> updatePlaylist(@PathVariable Long id,
                        @RequestPart("title") String title,
                        @RequestPart(value = "image", required = false) MultipartFile image) {
                try {
                        PlaylistUpdateDTO dto = new PlaylistUpdateDTO(title, image);
                        return playlistService.updatePlaylist(id, dto)
                                        .map(this::toPreviewResponseDTO)
                                        .map(ResponseEntity::ok)
                                        .orElse(ResponseEntity.notFound().build());
                } catch (NotFoundException e) {
                        logger.error("Playlist not found for update: {}", id, e);
                        return ResponseEntity.notFound().build();
                } catch (IOException | IllegalStateException e) {
                        logger.error("Error updating playlist: {}", id, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deletePlaylistById(@PathVariable Long id) {
                return playlistService.deletePlaylistById(id)
                                ? ResponseEntity.noContent().build()
                                : ResponseEntity.notFound().build();
        }
}

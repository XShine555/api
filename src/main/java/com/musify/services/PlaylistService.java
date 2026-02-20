package com.musify.services;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import com.musify.DTOs.Playlist.PlaylistUpdateDTO;
import com.musify.exceptions.NotFoundException;
import com.musify.models.Playlist;
import com.musify.repositories.PlaylistRepository;

import io.micrometer.common.util.StringUtils;

@Service
public class PlaylistService {
    private static final Path PLAYLIST_IMAGE_DIR = Path.of("private/images/playlists");

    private final UserService userService;
    private final PlaylistRepository playlistRepository;

    public PlaylistService(UserService userService, PlaylistRepository playlistRepository) {
        this.userService = userService;
        this.playlistRepository = playlistRepository;
    }

    public Playlist createPlaylist(Long id) throws NotFoundException {
        userService.getUserById(id).orElseThrow(() -> new NotFoundException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setUserId(id);
        playlist.setTitle("New Playlist");
        return playlistRepository.save(playlist);
    }

    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    public Optional<Playlist> getPlaylistById(Long id) {
        return playlistRepository.findById(id);
    }

    public List<Playlist> searchByName(String name) {
        return playlistRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<Playlist> updatePlaylist(Long id, PlaylistUpdateDTO dto) throws IOException, IllegalStateException {
        String extension = FilenameUtils.getExtension(dto.image().getOriginalFilename());
        Path imagePath = PLAYLIST_IMAGE_DIR.resolve(
                String.format("%s.%s", UUID.randomUUID(), extension));

        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Playlist not found"));

        if (StringUtils.isNotBlank(dto.title())) {
            playlist.setTitle(dto.title());
        }
        if (dto.image() != null && !dto.image().isEmpty()) {
            dto.image().transferTo(imagePath);
            playlist.setImagePath(imagePath.toString());
        }

        playlistRepository.save(playlist);

        return Optional.of(playlist);
    }

    public boolean deletePlaylistById(Long id) {
        if (!playlistRepository.existsById(id)) {
            return false;
        }
        playlistRepository.deleteById(id);
        return true;
    }
}

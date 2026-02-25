package com.musify.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.musify.DTOs.Playlist.PlaylistUpdateDTO;
import com.musify.exceptions.NotFoundException;
import com.musify.models.Playlist;
import com.musify.models.User;
import com.musify.repositories.PlaylistRepository;

import io.micrometer.common.util.StringUtils;

@Service
public class PlaylistService {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);
    private static final Path PLAYLIST_IMAGE_DIR = Path.of("/app/private/images/playlists");

    private final UserService userService;
    private final PlaylistRepository playlistRepository;

    public PlaylistService(UserService userService, PlaylistRepository playlistRepository) {
        this.userService = userService;
        this.playlistRepository = playlistRepository;
    }

    public Playlist createPlaylist(Long id) throws NotFoundException {
        User user = userService.getUserById(id).orElseThrow(() -> new NotFoundException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setTitle("New Playlist");
        return playlistRepository.save(playlist);
    }

    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    public Optional<Playlist> getPlaylistById(Long id) {
        return playlistRepository.findById(id);
    }

    public List<Playlist> searchByTitle(String title) {
        return playlistRepository.findByTitleContainingIgnoreCase(title);
    }

    public Optional<Playlist> updatePlaylist(Long id, PlaylistUpdateDTO dto) throws IOException, IllegalStateException {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Playlist not found"));

        if (StringUtils.isNotBlank(dto.title())) {
            playlist.setTitle(dto.title());
            logger.info("Updated playlist title: {}", dto.title());
        }
        if (dto.image() != null && !dto.image().isEmpty()) {
            Files.createDirectories(PLAYLIST_IMAGE_DIR);
            String originalName = dto.image().getOriginalFilename();
            if (originalName == null) {
                originalName = UUID.randomUUID().toString();
                logger.warn("Original filename is empty, generated random name: {}", originalName);
            }

            String extension = FilenameUtils.getExtension(originalName);
            Files.createDirectories(PLAYLIST_IMAGE_DIR);

            String imageName = String.format("%s.%s", UUID.randomUUID(), extension);
            Path imagePath = PLAYLIST_IMAGE_DIR.resolve(imageName);

            logger.info("Saving playlist image to: {}", imagePath);
            dto.image().transferTo(imagePath.toFile());
            playlist.setImagePath(imageName);
            logger.info("Updated playlist image path: {}", playlist.getImagePath());
        }

        logger.info("Saving playlist with ID {}: title='{}', imagePath='{}'", playlist.getId(), playlist.getTitle(), playlist.getImagePath());
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

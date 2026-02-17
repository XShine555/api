package com.musify.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.musify.DTOs.Playlist.PlaylistCreateDTO;
import com.musify.DTOs.Playlist.PlaylistUpdateDTO;
import com.musify.models.Playlist;
import com.musify.repositories.PlaylistRepository;

import io.micrometer.common.util.StringUtils;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    public Playlist createPlaylist(PlaylistCreateDTO dto) {
        Playlist playlist = new Playlist();
        playlist.setName(dto.name());
        playlist.setImagePath(dto.imagePath());
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

    public Optional<Playlist> updatePlaylist(Long id, PlaylistUpdateDTO dto) {
        return playlistRepository.findById(id).map(playlist -> {
            if (StringUtils.isNotBlank(dto.name())) {
                playlist.setName(dto.name());
            }
            if (StringUtils.isNotBlank(dto.imagePath())) {
                playlist.setImagePath(dto.imagePath());
            }
            return playlistRepository.save(playlist);
        });
    }

    public boolean deletePlaylistById(Long id) {
        if (!playlistRepository.existsById(id)) {
            return false;
        }
        playlistRepository.deleteById(id);
        return true;
    }
}

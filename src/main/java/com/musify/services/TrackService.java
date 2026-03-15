package com.musify.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.musify.models.Track;
import com.musify.repositories.TrackRepository;

@Service
public class TrackService {

    private static final Logger logger = LoggerFactory.getLogger(TrackService.class);

    private final TrackRepository trackRepository;

    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public List<Track> getAllTracks() {
        logger.debug("Retrieving all tracks");
        return trackRepository.findAll();
    }

    public Optional<Track> getTrackById(Long id) {
        logger.debug("Retrieving track with ID: {}", id);
        return trackRepository.findById(id);
    }
}

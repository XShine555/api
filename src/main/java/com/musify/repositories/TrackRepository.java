package com.musify.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musify.models.Track;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
}

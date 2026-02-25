package com.musify.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musify.models.PlaylistTrack;
import com.musify.models.PlaylistTrackId;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrackId> {

    @Query("""
            SELECT pt
            FROM PlaylistTrack pt
            JOIN FETCH pt.track t
            JOIN FETCH t.artist
            WHERE pt.playlist.id = :playlistId
            """)
    List<PlaylistTrack> findFilteredByPlaylistId(
            @Param("playlistId") Long playlistId,
            Sort sort);
}

package com.musify.models;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

@Embeddable
public class PlaylistTrackId implements Serializable {
    private Long playlistId;
    private Long trackId;
    
    public Long getPlaylistId() {
        return playlistId;
    }
    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }
    public Long getTrackId() {
        return trackId;
    }
    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }
}
package com.musicwise.dto;

import java.util.List;

public class TrackResponse {
    private String vibe;
    private List<TrackItem> tracks;

    public TrackResponse() {
    }

    public TrackResponse(String vibe, List<TrackItem> tracks) {
        this.vibe = vibe;
        this.tracks = tracks;
    }

    public String getVibe() {
        return vibe;
    }

    public void setVibe(String vibe) {
        this.vibe = vibe;
    }

    public List<TrackItem> getTracks() {
        return tracks;
    }

    public void setTracks(List<TrackItem> tracks) {
        this.tracks = tracks;
    }
}

package com.musicwise.dto;

import java.util.List;

public class MusicSearchResponse {
    private List<MusicTrackItem> tracks;

    public MusicSearchResponse() {
    }

    public MusicSearchResponse(List<MusicTrackItem> tracks) {
        this.tracks = tracks;
    }

    public List<MusicTrackItem> getTracks() {
        return tracks;
    }

    public void setTracks(List<MusicTrackItem> tracks) {
        this.tracks = tracks;
    }
}

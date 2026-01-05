package com.musicwise.service;

import com.musicwise.dto.TrackItem;
import com.musicwise.dto.TrackResponse;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TrackService {
    private final Map<String, List<TrackItem>> tracks = Map.of(
            "warm", List.of(new TrackItem(1L, "https://example.com/audio-warm.mp3", 12)),
            "calm", List.of(new TrackItem(2L, "https://example.com/audio-calm.mp3", 12)),
            "power", List.of(new TrackItem(3L, "https://example.com/audio-power.mp3", 12))
    );

    public TrackResponse getTracks(String vibe) {
        String resolved = tracks.containsKey(vibe) ? vibe : "warm";
        return new TrackResponse(resolved, tracks.get(resolved));
    }

    public TrackItem pickTrack(String vibe) {
        String resolved = tracks.containsKey(vibe) ? vibe : "warm";
        return tracks.get(resolved).get(0);
    }
}



















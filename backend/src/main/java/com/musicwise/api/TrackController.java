package com.musicwise.api;

import com.musicwise.dto.TrackResponse;
import com.musicwise.service.TrackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TrackController {
    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/tracks")
    public TrackResponse getTracks(@RequestParam(name = "vibe", defaultValue = "warm") String vibe) {
        return trackService.getTracks(vibe);
    }
}

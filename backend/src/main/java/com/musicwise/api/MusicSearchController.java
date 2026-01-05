package com.musicwise.api;

import com.musicwise.dto.MusicSearchResponse;
import com.musicwise.service.MusicSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/music")
public class MusicSearchController {
    private static final Logger log = LoggerFactory.getLogger(MusicSearchController.class);
    private final MusicSearchService musicSearchService;

    public MusicSearchController(MusicSearchService musicSearchService) {
        this.musicSearchService = musicSearchService;
    }

    @GetMapping("/search")
    public MusicSearchResponse search(@RequestParam("q") String query,
                                      @RequestParam(name = "limit", defaultValue = "3") int limit) {
        log.info("music search request q={} limit={}", query, limit);
        return musicSearchService.search(query, limit);
    }
}

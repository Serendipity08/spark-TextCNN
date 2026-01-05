package com.musicwise.api;

import com.musicwise.dto.MixResponse;
import com.musicwise.service.MixService;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class MixController {

    private final MixService mixService;

    public MixController(MixService mixService) {
        this.mixService = mixService;
    }

    @PostMapping(value = "/mix", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MixResponse> mix(
            @RequestParam("tts") MultipartFile ttsFile,
            @RequestParam(value = "music", required = false) MultipartFile musicFile,
            @RequestParam(value = "musicUrl", required = false) String musicUrl,
            @RequestParam(value = "musicVolume", required = false) Double musicVolume,
            @RequestParam(value = "targetDuration", required = false) Double targetDuration
    ) {
        try {
            MixResponse response = mixService.mix(ttsFile, musicFile, musicUrl, musicVolume, targetDuration);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

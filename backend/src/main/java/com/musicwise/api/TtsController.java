package com.musicwise.api;

import com.musicwise.dto.TtsRequest;
import com.musicwise.dto.TtsResponse;
import com.musicwise.service.TtsService;
import java.util.Base64;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TtsController {
    private final TtsService ttsService;

    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping("/tts")
    public TtsResponse synthesize(@RequestBody TtsRequest request) {
        try {
            TtsService.TtsResult result = ttsService.synthesize(request == null ? "" : request.getContent());
            String audioBase64 = Base64.getEncoder().encodeToString(result.getAudioBytes());
            return new TtsResponse(audioBase64, result.getContentType());
        } catch (Exception ex) {
            TtsResponse response = new TtsResponse();
            response.setError(ex.getMessage());
            return response;
        }
    }
}

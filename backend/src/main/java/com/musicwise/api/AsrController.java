package com.musicwise.api;

import com.musicwise.dto.AsrResponse;
import com.musicwise.service.AsrService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class AsrController {
    private final AsrService asrService;

    public AsrController(AsrService asrService) {
        this.asrService = asrService;
    }

    @PostMapping(value = "/asr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AsrResponse transcribe(@RequestPart("file") MultipartFile file) {
        try {
            return new AsrResponse(asrService.transcribe(file));
        } catch (Exception ex) {
            AsrResponse response = new AsrResponse("");
            response.setError(ex.getMessage());
            return response;
        }
    }
}

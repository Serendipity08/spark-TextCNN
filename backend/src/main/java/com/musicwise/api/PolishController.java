package com.musicwise.api;

import com.musicwise.dto.PolishRequest;
import com.musicwise.dto.PolishResponse;
import com.musicwise.model.PolishTemplate;
import com.musicwise.service.PolishService;
import com.musicwise.service.PolishTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PolishController {
    private final PolishService polishService;
    private final PolishTemplateService polishTemplateService;

    public PolishController(PolishService polishService, PolishTemplateService polishTemplateService) {
        this.polishService = polishService;
        this.polishTemplateService = polishTemplateService;
    }

    @PostMapping("/polish")
    public PolishResponse polish(@Valid @RequestBody PolishRequest request) {
        return polishService.polish(request);
    }

    @GetMapping("/polish/templates")
    public List<PolishTemplate> templates() {
        return polishTemplateService.getTemplates();
    }
}









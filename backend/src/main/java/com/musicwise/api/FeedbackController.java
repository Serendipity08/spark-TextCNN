package com.musicwise.api;

import com.musicwise.dto.FeedbackResponse;
import com.musicwise.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/gifts/{giftId}/ack")
    public ResponseEntity<FeedbackResponse> ack(@PathVariable String giftId) {
        boolean updated = feedbackService.markCompleted(giftId);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new FeedbackResponse(true));
    }

    @PostMapping("/gifts/{giftId}/like")
    public ResponseEntity<FeedbackResponse> like(@PathVariable String giftId) {
        boolean updated = feedbackService.markLiked(giftId);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new FeedbackResponse(true));
    }
}

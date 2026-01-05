package com.musicwise.service;

import org.springframework.stereotype.Service;

@Service
public class FeedbackService {
    private final GiftService giftService;

    public FeedbackService(GiftService giftService) {
        this.giftService = giftService;
    }

    public boolean markCompleted(String giftId) {
        return giftService.markCompleted(giftId);
    }

    public boolean markLiked(String giftId) {
        return giftService.markLiked(giftId);
    }
}

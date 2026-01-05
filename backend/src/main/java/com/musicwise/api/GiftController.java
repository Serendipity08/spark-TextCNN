package com.musicwise.api;

import com.musicwise.dto.GiftCreateRequest;
import com.musicwise.dto.GiftCreateResponse;
import com.musicwise.dto.GiftDetailResponse;
import com.musicwise.model.Gift;
import com.musicwise.service.GiftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class GiftController {
    private final GiftService giftService;

    public GiftController(GiftService giftService) {
        this.giftService = giftService;
    }

    @PostMapping("/gifts")
    public GiftCreateResponse createGift(@Valid @RequestBody GiftCreateRequest request) {
        Gift gift = giftService.createGift(request);
        String receiverPath = "/receiver.html?giftId=" + gift.getGiftId();
        return new GiftCreateResponse(gift.getGiftId(), receiverPath);
    }

    @GetMapping("/gifts/{giftId}")
    public ResponseEntity<GiftDetailResponse> getGift(@PathVariable String giftId) {
        Gift gift = giftService.getGift(giftId);
        if (gift == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        giftService.markOpened(giftId);
        GiftDetailResponse response = new GiftDetailResponse(
                gift.getGiftId(),
                gift.getContentFinal(),
                gift.getVibe(),
                gift.getTrackUrl(),
                gift.getStartAt(),
                gift.getMusicId(),
                gift.getMusicTitle(),
                gift.getMusicArtist(),
                gift.getMusicPreviewUrl(),
                gift.getStatus().name()
        );
        return ResponseEntity.ok(response);
    }
}


















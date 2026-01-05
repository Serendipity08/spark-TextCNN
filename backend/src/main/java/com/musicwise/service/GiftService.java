package com.musicwise.service;

import com.musicwise.dto.TrackItem;
import com.musicwise.model.Gift;
import com.musicwise.model.GiftStatus;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GiftService {
    private final Map<String, Gift> store = new ConcurrentHashMap<>();
    private final TrackService trackService;

    public GiftService(TrackService trackService) {
        this.trackService = trackService;
    }

    public Gift createGift(com.musicwise.dto.GiftCreateRequest request) {
        String giftId = "gift_" + UUID.randomUUID();
        TrackItem track = trackService.pickTrack(request.getVibe());

        Gift gift = new Gift();
        gift.setGiftId(giftId);
        gift.setContentFinal(request.getContent());
        gift.setMode(request.getMode());
        gift.setVibe(request.getVibe());
        boolean useCustomMusic = StringUtils.hasText(request.getMusicPreviewUrl())
                || StringUtils.hasText(request.getMusicId())
                || StringUtils.hasText(request.getMusicTitle())
                || StringUtils.hasText(request.getMusicArtist());

        if (useCustomMusic) {
            gift.setTrackUrl(StringUtils.hasText(request.getMusicPreviewUrl()) ? request.getMusicPreviewUrl() : track.getUrl());
            gift.setStartAt(request.getMusicStartAt() != null ? request.getMusicStartAt() : track.getStartAt());
            gift.setMusicId(request.getMusicId());
            gift.setMusicTitle(request.getMusicTitle());
            gift.setMusicArtist(request.getMusicArtist());
            gift.setMusicPreviewUrl(request.getMusicPreviewUrl());
        } else {
            gift.setTrackUrl(track.getUrl());
            gift.setStartAt(track.getStartAt());
            gift.setMusicId(String.valueOf(track.getId()));
            gift.setMusicTitle("推荐曲目");
            gift.setMusicArtist("系统推荐");
            gift.setMusicPreviewUrl(track.getUrl());
        }
        gift.setStatus(GiftStatus.CREATED);

        store.put(giftId, gift);
        return gift;
    }

    public Gift getGift(String giftId) {
        return store.get(giftId);
    }

    public boolean markOpened(String giftId) {
        Gift gift = store.get(giftId);
        if (gift == null) {
            return false;
        }
        if (gift.getStatus() == GiftStatus.CREATED) {
            gift.setStatus(GiftStatus.OPENED);
        }
        return true;
    }

    public boolean markCompleted(String giftId) {
        Gift gift = store.get(giftId);
        if (gift == null) {
            return false;
        }
        gift.setStatus(GiftStatus.COMPLETED);
        return true;
    }

    public boolean markLiked(String giftId) {
        Gift gift = store.get(giftId);
        if (gift == null) {
            return false;
        }
        gift.setStatus(GiftStatus.LIKED);
        return true;
    }
}


















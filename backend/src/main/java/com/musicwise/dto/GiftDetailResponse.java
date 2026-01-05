package com.musicwise.dto;

public class GiftDetailResponse {
    private String giftId;
    private String content;
    private String vibe;
    private String trackUrl;
    private int startAt;
    private String musicId;
    private String musicTitle;
    private String musicArtist;
    private String musicPreviewUrl;
    private String status;

    public GiftDetailResponse() {
    }

    public GiftDetailResponse(String giftId, String content, String vibe, String trackUrl, int startAt,
                              String musicId, String musicTitle, String musicArtist, String musicPreviewUrl,
                              String status) {
        this.giftId = giftId;
        this.content = content;
        this.vibe = vibe;
        this.trackUrl = trackUrl;
        this.startAt = startAt;
        this.musicId = musicId;
        this.musicTitle = musicTitle;
        this.musicArtist = musicArtist;
        this.musicPreviewUrl = musicPreviewUrl;
        this.status = status;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVibe() {
        return vibe;
    }

    public void setVibe(String vibe) {
        this.vibe = vibe;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public String getMusicPreviewUrl() {
        return musicPreviewUrl;
    }

    public void setMusicPreviewUrl(String musicPreviewUrl) {
        this.musicPreviewUrl = musicPreviewUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


















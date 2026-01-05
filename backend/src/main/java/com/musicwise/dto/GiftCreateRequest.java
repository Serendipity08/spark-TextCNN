package com.musicwise.dto;

import jakarta.validation.constraints.NotBlank;

public class GiftCreateRequest {
    @NotBlank
    private String content;

    @NotBlank
    private String vibe;

    @NotBlank
    private String mode;
    private String musicId;
    private String musicTitle;
    private String musicArtist;
    private String musicPreviewUrl;
    private Integer musicStartAt;

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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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

    public Integer getMusicStartAt() {
        return musicStartAt;
    }

    public void setMusicStartAt(Integer musicStartAt) {
        this.musicStartAt = musicStartAt;
    }
}


















package com.musicwise.dto;

public class MusicTrackItem {
    private String id;
    private String title;
    private String artist;
    private String previewUrl;
    private Integer startAt;

    public MusicTrackItem() {
    }

    public MusicTrackItem(String id, String title, String artist, String previewUrl, Integer startAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.previewUrl = previewUrl;
        this.startAt = startAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Integer getStartAt() {
        return startAt;
    }

    public void setStartAt(Integer startAt) {
        this.startAt = startAt;
    }
}

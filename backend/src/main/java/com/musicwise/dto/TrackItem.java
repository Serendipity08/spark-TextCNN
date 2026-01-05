package com.musicwise.dto;

public class TrackItem {
    private long id;
    private String url;
    private int startAt;

    public TrackItem() {
    }

    public TrackItem(long id, String url, int startAt) {
        this.id = id;
        this.url = url;
        this.startAt = startAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }
}

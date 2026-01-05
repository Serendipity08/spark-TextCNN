package com.musicwise.dto;

public class TtsRequest {
    private String content;

    public TtsRequest() {
    }

    public TtsRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

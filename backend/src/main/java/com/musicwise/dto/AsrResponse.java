package com.musicwise.dto;

public class AsrResponse {
    private String text;
    private String error;

    public AsrResponse() {
    }

    public AsrResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

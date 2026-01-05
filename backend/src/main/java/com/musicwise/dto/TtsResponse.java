package com.musicwise.dto;

public class TtsResponse {
    private String audioBase64;
    private String audioType;
    private String error;

    public TtsResponse() {
    }

    public TtsResponse(String audioBase64, String audioType) {
        this.audioBase64 = audioBase64;
        this.audioType = audioType;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getAudioType() {
        return audioType;
    }

    public void setAudioType(String audioType) {
        this.audioType = audioType;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

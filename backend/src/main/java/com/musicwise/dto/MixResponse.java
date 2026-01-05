package com.musicwise.dto;

public class MixResponse {
    private String audioBase64;
    private String audioType;

    public MixResponse() {
    }

    public MixResponse(String audioBase64, String audioType) {
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
}


package com.musicwise.dto;

public class AsrHealthResponse {
    private boolean ffmpegAvailable;
    private String ffmpegPath;
    private boolean appIdConfigured;
    private boolean apiKeyConfigured;
    private boolean apiSecretConfigured;
    private String apiTestResult;
    private String message;

    public boolean isFfmpegAvailable() {
        return ffmpegAvailable;
    }

    public void setFfmpegAvailable(boolean ffmpegAvailable) {
        this.ffmpegAvailable = ffmpegAvailable;
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public boolean isAppIdConfigured() {
        return appIdConfigured;
    }

    public void setAppIdConfigured(boolean appIdConfigured) {
        this.appIdConfigured = appIdConfigured;
    }

    public boolean isApiKeyConfigured() {
        return apiKeyConfigured;
    }

    public void setApiKeyConfigured(boolean apiKeyConfigured) {
        this.apiKeyConfigured = apiKeyConfigured;
    }

    public boolean isApiSecretConfigured() {
        return apiSecretConfigured;
    }

    public void setApiSecretConfigured(boolean apiSecretConfigured) {
        this.apiSecretConfigured = apiSecretConfigured;
    }

    public String getApiTestResult() {
        return apiTestResult;
    }

    public void setApiTestResult(String apiTestResult) {
        this.apiTestResult = apiTestResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.musicwise.dto;

public class FeedbackResponse {
    private boolean success;

    public FeedbackResponse() {
    }

    public FeedbackResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

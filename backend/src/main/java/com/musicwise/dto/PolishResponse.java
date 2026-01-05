package com.musicwise.dto;

public class PolishResponse {
    private String combinedText;
    private String polishedText;

    public PolishResponse() {
    }

    public PolishResponse(String combinedText, String polishedText) {
        this.combinedText = combinedText;
        this.polishedText = polishedText;
    }

    public String getCombinedText() {
        return combinedText;
    }

    public void setCombinedText(String combinedText) {
        this.combinedText = combinedText;
    }

    public String getPolishedText() {
        return polishedText;
    }

    public void setPolishedText(String polishedText) {
        this.polishedText = polishedText;
    }
}

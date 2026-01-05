package com.musicwise.dto;

import com.musicwise.model.BlessingPreset;
import com.musicwise.model.RecipientType;
import jakarta.validation.constraints.NotBlank;

public class PolishRequest {
    private String content;

    @NotBlank
    private String mode;

    private RecipientType recipientType;
    private BlessingPreset blessingPreset;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
    }

    public BlessingPreset getBlessingPreset() {
        return blessingPreset;
    }

    public void setBlessingPreset(BlessingPreset blessingPreset) {
        this.blessingPreset = blessingPreset;
    }
}

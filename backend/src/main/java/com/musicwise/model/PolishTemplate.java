package com.musicwise.model;

public class PolishTemplate {
    private final String id;
    private final String title;
    private final String content;
    private final RecipientType recipientType;
    private final BlessingPreset blessingPreset;
    private final String tone;

    public PolishTemplate(String id, String title, String content,
                          RecipientType recipientType, BlessingPreset blessingPreset, String tone) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.recipientType = recipientType;
        this.blessingPreset = blessingPreset;
        this.tone = tone;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public BlessingPreset getBlessingPreset() {
        return blessingPreset;
    }

    public String getTone() {
        return tone;
    }
}

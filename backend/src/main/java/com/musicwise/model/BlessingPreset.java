package com.musicwise.model;

public enum BlessingPreset {
    BIRTHDAY("生日快乐"),
    FESTIVAL("节日快乐"),
    THANKS("感谢"),
    CONGRATS("祝贺"),
    NONE("无");

    private final String label;

    BlessingPreset(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

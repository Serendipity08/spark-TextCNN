package com.musicwise.model;

public enum RecipientType {
    FAMILY("家人"),
    CLASSMATE("同学"),
    FRIEND("朋友"),
    LOVER("爱人"),
    OTHER("其他"),
    NONE("无");

    private final String label;

    RecipientType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

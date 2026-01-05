package com.musicwise.dto;

public class GiftCreateResponse {
    private String giftId;
    private String receiverPath;

    public GiftCreateResponse() {
    }

    public GiftCreateResponse(String giftId, String receiverPath) {
        this.giftId = giftId;
        this.receiverPath = receiverPath;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public String getReceiverPath() {
        return receiverPath;
    }

    public void setReceiverPath(String receiverPath) {
        this.receiverPath = receiverPath;
    }
}

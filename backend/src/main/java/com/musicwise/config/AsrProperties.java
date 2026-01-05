package com.musicwise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "asr")
public class AsrProperties {
    private String provider = "xfyun"; // 默认使用讯飞，可改为baidu
    private XunfeiConfig xfyun;
    private BaiduConfig baidu;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public XunfeiConfig getXfyun() {
        return xfyun;
    }

    public void setXfyun(XunfeiConfig xfyun) {
        this.xfyun = xfyun;
    }

    public BaiduConfig getBaidu() {
        return baidu;
    }

    public void setBaidu(BaiduConfig baidu) {
        this.baidu = baidu;
    }

    public static class XunfeiConfig {
        private String appId;
        private String apiKey;
        private String apiSecret;

        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getApiSecret() { return apiSecret; }
        public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
    }

    public static class BaiduConfig {
        private String apiKey;
        private String secretKey;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }
}

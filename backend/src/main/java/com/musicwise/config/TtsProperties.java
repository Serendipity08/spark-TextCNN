package com.musicwise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tts")
public class TtsProperties {
    private String provider = "baidu";
    private BaiduConfig baidu = new BaiduConfig();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public BaiduConfig getBaidu() {
        return baidu;
    }

    public void setBaidu(BaiduConfig baidu) {
        this.baidu = baidu;
    }

    public static class BaiduConfig {
        private String apiKey;
        private String secretKey;
        private String cuid = "music_wise_app";
        private Integer per = 1;
        private Integer spd = 5;
        private Integer pit = 5;
        private Integer vol = 5;
        private Integer aue = 3;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getCuid() {
            return cuid;
        }

        public void setCuid(String cuid) {
            this.cuid = cuid;
        }

        public Integer getPer() {
            return per;
        }

        public void setPer(Integer per) {
            this.per = per;
        }

        public Integer getSpd() {
            return spd;
        }

        public void setSpd(Integer spd) {
            this.spd = spd;
        }

        public Integer getPit() {
            return pit;
        }

        public void setPit(Integer pit) {
            this.pit = pit;
        }

        public Integer getVol() {
            return vol;
        }

        public void setVol(Integer vol) {
            this.vol = vol;
        }

        public Integer getAue() {
            return aue;
        }

        public void setAue(Integer aue) {
            this.aue = aue;
        }
    }
}

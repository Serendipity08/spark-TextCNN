package com.musicwise.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicwise.config.TtsProperties;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TtsService {
    private static final Logger logger = LoggerFactory.getLogger(TtsService.class);
    private static final String BAIDU_TTS_URL = "https://tsn.baidu.com/text2audio";

    private final TtsProperties ttsProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public TtsService(TtsProperties ttsProperties, ObjectMapper objectMapper) {
        this.ttsProperties = ttsProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public TtsResult synthesize(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("文字内容不能为空");
        }

        String provider = ttsProperties.getProvider();
        if (!"baidu".equalsIgnoreCase(provider)) {
            throw new IllegalStateException("暂不支持该语音合成服务");
        }

        TtsProperties.BaiduConfig baidu = ttsProperties.getBaidu();
        if (baidu == null || isBlank(baidu.getApiKey()) || isBlank(baidu.getSecretKey())) {
            throw new IllegalStateException("未配置百度语音合成API参数");
        }

        try {
            String token = getBaiduAccessToken(baidu.getApiKey(), baidu.getSecretKey());
            return callBaiduTts(token, content, baidu);
        } catch (Exception ex) {
            logger.error("百度TTS调用失败: {}", ex.getMessage());
            throw new IllegalStateException("语音合成失败，请稍后再试");
        }
    }

    private TtsResult callBaiduTts(String token, String content, TtsProperties.BaiduConfig config) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("tex", doubleUrlEncode(content));
        params.put("tok", token);
        params.put("cuid", defaultValue(config.getCuid(), "music_wise_app"));
        params.put("ctp", "1");
        params.put("lan", "zh");
        putIfPresent(params, "per", config.getPer());
        putIfPresent(params, "spd", config.getSpd());
        putIfPresent(params, "pit", config.getPit());
        putIfPresent(params, "vol", config.getVol());
        putIfPresent(params, "aue", config.getAue());

        String body = buildFormBody(params);
        HttpRequest request = HttpRequest.newBuilder(URI.create(BAIDU_TTS_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (contentType.startsWith("audio")) {
            return new TtsResult(response.body(), contentType);
        }

        String errorPayload = new String(response.body(), StandardCharsets.UTF_8);
        String message = parseBaiduError(errorPayload);
        throw new IllegalStateException(message);
    }

    private String getBaiduAccessToken(String apiKey, String secretKey) throws Exception {
        String url = "https://aip.baidubce.com/oauth/2.0/token";
        String params = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s",
                URLEncoder.encode(apiKey, StandardCharsets.UTF_8),
                URLEncoder.encode(secretKey, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(params))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("获取百度Access Token失败");
        }

        JsonNode root = objectMapper.readTree(response.body());
        String accessToken = root.path("access_token").asText();
        if (isBlank(accessToken)) {
            throw new IllegalStateException("获取百度Access Token失败");
        }
        return accessToken;
    }

    private String parseBaiduError(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String errorMessage = root.path("err_msg").asText("");
            String errorNo = root.path("err_no").asText("");
            if (!errorMessage.isBlank()) {
                return "语音合成失败: " + errorMessage + "(" + errorNo + ")";
            }
        } catch (Exception ignored) {
        }
        return "语音合成失败";
    }

    private String doubleUrlEncode(String value) {
        String once = URLEncoder.encode(value, StandardCharsets.UTF_8);
        return URLEncoder.encode(once, StandardCharsets.UTF_8);
    }

    private String buildFormBody(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }

    private void putIfPresent(Map<String, String> params, String key, Integer value) {
        if (value == null) {
            return;
        }
        params.put(key, String.valueOf(value));
    }

    private String defaultValue(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class TtsResult {
        private final byte[] audioBytes;
        private final String contentType;

        public TtsResult(byte[] audioBytes, String contentType) {
            this.audioBytes = audioBytes;
            this.contentType = contentType;
        }

        public byte[] getAudioBytes() {
            return audioBytes;
        }

        public String getContentType() {
            return contentType;
        }
    }
}

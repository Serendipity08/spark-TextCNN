package com.musicwise.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicwise.dto.MusicSearchResponse;
import com.musicwise.dto.MusicTrackItem;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class MusicSearchService {
    private static final Logger log = LoggerFactory.getLogger(MusicSearchService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public MusicSearchService(
            @Value("${doubao.music.base-url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}") String baseUrl,
            @Value("${doubao.music.api-key:}") String apiKey,
            @Value("${doubao.music.model:ep-20251230101646-9pck8}") String model,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
    }

    public MusicSearchResponse search(String query, int limit) {
        if (!StringUtils.hasText(query)) {
            return new MusicSearchResponse(Collections.emptyList());
        }
        int resolvedLimit = Math.max(1, Math.min(limit, 3));
        List<MusicTrackItem> tracks = callDoubao(query, resolvedLimit);
        if (tracks.size() < resolvedLimit) {
            tracks.addAll(fallbackTracks(resolvedLimit - tracks.size()));
        }
        return new MusicSearchResponse(tracks);
    }

    private List<MusicTrackItem> callDoubao(String query, int limit) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content",
                        "You are a music search helper. Only return a JSON array, no extra text. "
                                + "Each item must include id, title, artist, previewUrl, startAt (seconds, integer). "
                                + "If previewUrl is unavailable, use an empty string."),
                Map.of("role", "user", "content",
                        "Recommend " + limit + " songs related to \"" + query + "\". "
                                + "Return only a JSON array with fields id,title,artist,previewUrl,startAt. Example: "
                                + "[{\"id\":\"1\",\"title\":\"Sample\",\"artist\":\"Artist\",\"previewUrl\":\"https://...\",\"startAt\":12}]")
        ));

        try {
            ResponseEntity<DoubaoChatResponse> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, new HttpEntity<>(body, headers), DoubaoChatResponse.class);
            log.info("doubao status={} body={}", response.getStatusCode(), response.getBody());
            String content = extractContent(response.getBody());
            log.info("doubao content: {}", content);
            List<MusicTrackItem> parsed = parseContentToTracks(content, limit);
            if (parsed != null) {
                return parsed;
            }
        } catch (Exception ignored) {
            log.warn("doubao request failed, returning empty list", ignored);
        }
        return Collections.emptyList();
    }

    private String extractContent(DoubaoChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "";
        }
        return response.getChoices().get(0).getMessage().getContent();
    }

    private List<MusicTrackItem> parseContentToTracks(String content, int limit) {
        if (!StringUtils.hasText(content)) {
            return Collections.emptyList();
        }
        String cleaned = stripCodeFence(content.trim());
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(cleaned, new TypeReference<>() {
            });
            return toTracks(raw, limit);
        } catch (Exception ignored) {
            // continue
        }
        try {
            Map<String, Object> obj = objectMapper.readValue(cleaned, new TypeReference<>() {
            });
            Object tracksObj = obj.get("tracks");
            if (tracksObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> raw = (List<Map<String, Object>>) tracksObj;
                return toTracks(raw, limit);
            }
        } catch (Exception ignored) {
            // continue
        }
        return Collections.emptyList();
    }

    private String stripCodeFence(String text) {
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            }
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    private List<MusicTrackItem> toTracks(List<Map<String, Object>> raw, int limit) {
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream()
                .filter(Objects::nonNull)
                .limit(limit)
                .map(item -> new MusicTrackItem(
                        stringVal(item.getOrDefault("id", "")),
                        stringVal(item.getOrDefault("title", "")),
                        stringVal(item.getOrDefault("artist", "")),
                        stringVal(item.getOrDefault("previewUrl", "")),
                        intVal(item.get("startAt"))
                ))
                .collect(Collectors.toList());
    }

    private List<MusicTrackItem> fallbackTracks(int needed) {
        if (needed <= 0) {
            return Collections.emptyList();
        }
        return List.of(
                new MusicTrackItem("fallback-1", "Calm Breeze", "System", "https://file-examples.com/storage/fe9afc20e181b42c50d7784/2017/11/file_example_MP3_700KB.mp3", 0),
                new MusicTrackItem("fallback-2", "Soft Night", "System", "https://file-examples.com/storage/fe9afc20e181b42c50d7784/2017/11/file_example_MP3_2MG.mp3", 0),
                new MusicTrackItem("fallback-3", "Bright Day", "System", "https://file-examples.com/storage/fe9afc20e181b42c50d7784/2017/11/file_example_MP3_5MG.mp3", 0)
        ).stream().limit(needed).collect(Collectors.toList());
    }

    private String stringVal(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    private Integer intVal(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static class DoubaoChatResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    public static class Message {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

package com.musicwise.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.musicwise.config.AiPolishProperties;
import java.util.List;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SiliconFlowClient {
    private static final String SYSTEM_PROMPT = "你是一名擅长把普通祝福改写成「有温度、像真实的人在说话」的文字润色助手。请在【不使用模板化祝福语、不并列多个节日、不重复“祝你快乐”句式】的前提下 将下面这段话润色为一段更自然、更真诚、适合被朗读并搭配背景音乐的表达。必须遵守以下要求1. 整段文字只能有一个清晰的情绪中心 重点放在“对这个人说”2. 如果涉及多个时间点或节日 需要自然融合 而不是拆成并列祝福3. 语言要具体、有画面感 避免空泛词汇如快乐、幸福、美好4. 句子长度要适合朗读有自然停顿读起来顺口5. 情绪克制、不煽情、不像贺卡或营销文案6. 总字数控制在 60到100 字之间7. 只输出最终润色后的文本，不要解释、不列举版本";

    private final RestTemplate restTemplate;
    private final AiPolishProperties properties;

    public SiliconFlowClient(RestTemplateBuilder builder, AiPolishProperties properties) {
        this.restTemplate = builder.build();
        this.properties = properties;
    }

    public String polish(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        if (!properties.isEnabled()) {
            return null;
        }
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        String endpoint = properties.getEndpoint();
        ChatRequest request = new ChatRequest(
                properties.getModel(),
                List.of(
                        new Message("system", SYSTEM_PROMPT),
                        new Message("user", content)
                ),
                properties.getTemperature(),
                properties.getMaxTokens()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());
        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(endpoint, entity, ChatResponse.class);
        if (response.getBody() == null || response.getBody().choices == null || response.getBody().choices.isEmpty()) {
            return null;
        }
        Message message = response.getBody().choices.get(0).message;
        if (message == null || message.content == null) {
            return null;
        }
        return message.content.trim();
    }

    private static class ChatRequest {
        private final String model;
        private final List<Message> messages;
        private final Double temperature;
        @JsonProperty("max_tokens")
        private final Integer maxTokens;

        private ChatRequest(String model, List<Message> messages, Double temperature, Integer maxTokens) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
            this.maxTokens = maxTokens;
        }

        public String getModel() {
            return model;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public Double getTemperature() {
            return temperature;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Message {
        private String role;
        private String content;

        public Message() {
        }

        private Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }
}

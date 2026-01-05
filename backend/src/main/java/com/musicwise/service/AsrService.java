package com.musicwise.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicwise.config.AsrProperties;
import com.musicwise.config.AsrRuntimeProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AsrService {
    // 讯飞语音识别配置
    private static final String ENGINE_TYPE = "sms16k";
    private static final String AUDIO_ENCODING = "raw";
    private static final int SAMPLE_RATE = 16000;

    private static final Logger logger = LoggerFactory.getLogger(AsrService.class);

    private final AsrProperties asrProperties;
    private final AsrRuntimeProperties runtimeProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AsrService(AsrProperties asrProperties, AsrRuntimeProperties runtimeProperties, ObjectMapper objectMapper) {
        this.asrProperties = asrProperties;
        this.runtimeProperties = runtimeProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String transcribe(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }

        String provider = asrProperties.getProvider();
        logger.info("使用语音识别服务提供商: {}", provider);

        try {
            if ("baidu".equals(provider)) {
                return transcribeWithBaiduAPI(file);
            } else {
                // 默认使用讯飞
                return transcribeWithXfyunAPI(file);
            }
        } catch (Exception e) {
            logger.error("{} ASR API调用失败: {}", provider, e.getMessage());
            throw new IllegalStateException("语音识别服务不可用，请检查API配置: " + e.getMessage());
        }
    }

    private String transcribeWithRestApi(MultipartFile file, String appId, String authKey) throws Exception {
        Path inputPath = null;
        Path outputPath = null;
        try {
            inputPath = Files.createTempFile("asr-input-", safeSuffix(file.getOriginalFilename()));
            outputPath = Files.createTempFile("asr-output-", ".pcm");
            file.transferTo(inputPath);
            long inputSize = Files.size(inputPath);
            if (inputSize < 2048) {
                throw new IllegalStateException("录音时间太短，请重新录制");
            }
            convertToPcm(inputPath, outputPath);

            byte[] audioBytes = Files.readAllBytes(outputPath);
            if (audioBytes.length == 0) {
                return "";
            }

            String paramJson = objectMapper.writeValueAsString(Map.of(
                    "engine_type", ENGINE_TYPE,
                    "aue", AUDIO_ENCODING,
                    "auf", "audio/L16;rate=" + SAMPLE_RATE,
                    "scene", "main"
            ));
            String paramBase64 = Base64.getEncoder().encodeToString(paramJson.getBytes(StandardCharsets.UTF_8));

            // 使用HMAC-SHA256认证
            String curTime = String.valueOf(Instant.now().getEpochSecond());
            String checkSum = hmacSha256(authKey, authKey + curTime + paramBase64);

            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
            String body = "audio=" + java.net.URLEncoder.encode(audioBase64, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.xfyun.cn/v1/service/v1/iat"))
                    .timeout(Duration.ofSeconds(30))
                    .header("X-Appid", appId)
                    .header("X-CurTime", curTime)
                    .header("X-Param", paramBase64)
                    .header("X-CheckSum", checkSum)
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("REST API响应 - 状态码: {}, 响应长度: {}", response.statusCode(), response.body().length());

            if (response.statusCode() != 200) {
                logger.error("REST API调用失败 - 响应: {}", response.body());
                throw new IllegalStateException("语音识别请求失败：" + response.statusCode()
                        + "，响应：" + response.body());
            }
            return parseResult(response.body());
        } catch (Exception ex) {
            logger.error("REST API语音识别失败", ex);
            throw new IllegalStateException("语音识别失败：" + ex.getMessage(), ex);
        } finally {
            deleteQuietly(inputPath);
            deleteQuietly(outputPath);
        }
    }

    private String parseResult(String payload) throws IOException {
        JsonNode root = objectMapper.readTree(payload);
        int code = root.path("code").asInt(-1);
        if (code != 0) {
            String message = root.path("message").asText("");
            if (message.isBlank()) {
                message = root.path("desc").asText("");
            }
            if (message.isBlank()) {
                message = "语音识别失败";
            }
            throw new IllegalStateException(message + "，响应：" + payload);
        }

        String result = root.path("data").path("result").asText("");
        if (result.isEmpty()) {
            return "";
        }

        try {
            JsonNode resultNode = objectMapper.readTree(result);
            StringBuilder sb = new StringBuilder();
            for (JsonNode ws : resultNode.path("ws")) {
                JsonNode cw = ws.path("cw");
                if (cw.isArray() && cw.size() > 0) {
                    sb.append(cw.get(0).path("w").asText(""));
                }
            }
            String text = sb.toString().trim();
            return text.isEmpty() ? result : text;
        } catch (Exception ex) {
            return result;
        }
    }

    /**
     * 使用讯飞REST API进行语音识别
     */
    private String transcribeWithXfyunAPI(MultipartFile file) throws Exception {
        // 检查讯飞配置
        AsrProperties.XunfeiConfig xfyun = asrProperties.getXfyun();
        if (xfyun == null || isBlank(xfyun.getAppId()) || isBlank(xfyun.getApiSecret())) {
            throw new IllegalStateException("未配置讯飞API参数");
        }

        String appId = xfyun.getAppId();
        String apiSecret = xfyun.getApiSecret();
        Path inputPath = null;
        Path outputPath = null;

        try {
            // 音频文件预处理
            inputPath = Files.createTempFile("asr-input-", safeSuffix(file.getOriginalFilename()));
            outputPath = Files.createTempFile("asr-output-", ".pcm");
            file.transferTo(inputPath);

            long inputSize = Files.size(inputPath);
            if (inputSize < 2048) {
                throw new IllegalStateException("录音时间太短，请重新录制");
            }

            convertToPcm(inputPath, outputPath);
            byte[] audioBytes = Files.readAllBytes(outputPath);
            if (audioBytes.length == 0) {
                return "";
            }

            // 构建讯飞REST API请求
            String paramJson = objectMapper.writeValueAsString(Map.of(
                    "engine_type", ENGINE_TYPE,
                    "aue", AUDIO_ENCODING,
                    "auf", "audio/L16;rate=" + SAMPLE_RATE,
                    "scene", "main"
            ));

            String paramBase64 = Base64.getEncoder().encodeToString(paramJson.getBytes(StandardCharsets.UTF_8));
            String curTime = String.valueOf(Instant.now().getEpochSecond());

                // 使用MD5摘要签名（REST API标准）
            String checkSum = md5Hex(apiSecret + curTime + paramBase64);
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
            String body = "audio=" + java.net.URLEncoder.encode(audioBase64, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.xfyun.cn/v1/service/v1/iat"))
                    .timeout(Duration.ofSeconds(30))
                    .header("X-Appid", appId)
                    .header("X-CurTime", curTime)
                    .header("X-Param", paramBase64)
                    .header("X-CheckSum", checkSum)
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("讯飞ASR API响应 - 状态码: {}, 长度: {}", response.statusCode(), response.body().length());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("API请求失败: " + response.statusCode() + " - " + response.body());
            }

            return parseResult(response.body());

        } finally {
            deleteQuietly(inputPath);
            deleteQuietly(outputPath);
        }
    }

    /**
     * 使用百度智能云API进行语音识别
     */
    private String transcribeWithBaiduAPI(MultipartFile file) throws Exception {
        // 检查百度配置
        AsrProperties.BaiduConfig baidu = asrProperties.getBaidu();
        if (baidu == null || isBlank(baidu.getApiKey()) || isBlank(baidu.getSecretKey())) {
            throw new IllegalStateException("未配置百度API参数");
        }

        Path inputPath = null;
        Path outputPath = null;

        try {
            // 音频文件预处理
            inputPath = Files.createTempFile("asr-input-", safeSuffix(file.getOriginalFilename()));
            outputPath = Files.createTempFile("asr-output-", ".pcm");
            file.transferTo(inputPath);

            long inputSize = Files.size(inputPath);
            if (inputSize < 2048) {
                throw new IllegalStateException("录音时间太短，请重新录制");
            }

            convertToPcm(inputPath, outputPath);
            byte[] audioBytes = Files.readAllBytes(outputPath);
            if (audioBytes.length == 0) {
                return "";
            }

            // 1. 获取Access Token
            String accessToken = getBaiduAccessToken(baidu.getApiKey(), baidu.getSecretKey());
            logger.info("成功获取百度Access Token");

            // 2. 调用语音识别API
            return callBaiduSpeechAPI(accessToken, audioBytes);

        } finally {
            deleteQuietly(inputPath);
            deleteQuietly(outputPath);
        }
    }

    /**
     * 获取百度Access Token
     */
    private String getBaiduAccessToken(String apiKey, String secretKey) throws Exception {
        String url = "https://aip.baidubce.com/oauth/2.0/token";
        String params = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s",
                                    java.net.URLEncoder.encode(apiKey, StandardCharsets.UTF_8),
                                    java.net.URLEncoder.encode(secretKey, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(params))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("获取百度Access Token失败: " + response.body());
        }

        // 解析JSON响应获取access_token
        JsonNode root = objectMapper.readTree(response.body());
        String accessToken = root.path("access_token").asText();

        if (isBlank(accessToken)) {
            throw new IllegalStateException("获取百度Access Token失败: " + response.body());
        }

        return accessToken;
    }

    /**
     * 调用百度语音识别API
     */
    private String callBaiduSpeechAPI(String accessToken, byte[] audioBytes) throws Exception {
        // 百度语音识别API - 尝试直接使用client_id和client_secret认证
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

        // 使用百度语音识别标准版API端点
        String url = "http://vop.baidu.com/server_api";

        // 构建JSON请求体（按照官方文档格式）
        String jsonBody = String.format(
            "{\"format\":\"pcm\",\"rate\":16000,\"dev_pid\":1537,\"channel\":1,\"token\":\"%s\",\"cuid\":\"music_wise_app\",\"len\":%d,\"speech\":\"%s\"}",
            accessToken,
            audioBytes.length,
            audioBase64
        );

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("百度ASR API响应 - 状态码: {}, 长度: {}", response.statusCode(), response.body().length());

        if (response.statusCode() != 200) {
            String errorMsg = "百度语音识别API调用失败: " + response.statusCode() + " - " + response.body();

            if (response.statusCode() == 404) {
                errorMsg += "\n可能原因：\n" +
                           "1. 百度AI开放平台语音识别服务未开通\n" +
                           "2. API密钥权限不足\n" +
                           "3. 需要在百度AI开放平台创建应用\n" +
                           "请访问 https://ai.baidu.com/ 检查服务状态和应用配置";
            } else if (response.body().contains("Unsupported openapi method")) {
                errorMsg += "\nAPI调用方式不正确，请检查百度AI开放平台文档";
            }

            throw new IllegalStateException(errorMsg);
        }

        // 解析响应
        JsonNode root = objectMapper.readTree(response.body());
        int errNo = root.path("err_no").asInt(-1);

        if (errNo != 0) {
            String errMsg = root.path("err_msg").asText("未知错误");
            throw new IllegalStateException("百度语音识别失败: " + errMsg);
        }

        // 获取识别结果
        JsonNode result = root.path("result");
        if (result.isArray() && result.size() > 0) {
            return result.get(0).asText("");
        }

        return "";
    }

    /**
     * 生成模拟转写结果（用于测试或API失败时的降级）
     */
    private String generateMockTranscription(MultipartFile file) {
        // 基于文件大小和名称生成模拟结果
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();

        // 简单的模拟逻辑
        if (fileSize < 10000) {
            return "你好，这是一段测试录音。";
        } else if (fileSize < 50000) {
            return "亲爱的朋友，祝你生日快乐！希望你天天开心，每一天都充满欢笑。";
        } else {
            return "感谢你一直以来的陪伴和支持。你的友谊对我来说非常珍贵，希望我们能一直保持联系。";
        }
    }



    private void convertToPcm(Path inputPath, Path outputPath) throws IOException, InterruptedException {
        String ffmpegPath = runtimeProperties.getFfmpegPath();
        ProcessBuilder builder = new ProcessBuilder(
                ffmpegPath,
                "-y",
                "-i", inputPath.toString(),
                "-ac", "1",
                "-ar", String.valueOf(SAMPLE_RATE),
                "-f", "s16le",
                outputPath.toString()
        );
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String logs = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("音频转码失败，请确认已安装 ffmpeg。日志：" + logs);
        }
    }


    private String md5Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("无法生成签名");
        }
    }

    // HMAC-SHA256签名方法
    private String hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new RuntimeException("无法生成HMAC-SHA256签名: " + ex.getMessage());
        }
    }


    private String safeSuffix(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".webm";
        }
        String suffix = filename.substring(filename.lastIndexOf('.'));
        return suffix.length() > 10 ? ".webm" : suffix;
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // ignore
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

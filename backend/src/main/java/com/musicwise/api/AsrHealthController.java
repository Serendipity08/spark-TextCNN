package com.musicwise.api;

import com.musicwise.config.AsrProperties;
import com.musicwise.config.AsrRuntimeProperties;
import com.musicwise.dto.AsrHealthResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AsrHealthController {
    private final AsrProperties asrProperties;
    private final AsrRuntimeProperties runtimeProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AsrHealthController(AsrProperties asrProperties, AsrRuntimeProperties runtimeProperties) {
        this.asrProperties = asrProperties;
        this.runtimeProperties = runtimeProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/asr/health")
    public AsrHealthResponse health() {
        String provider = asrProperties.getProvider();

        boolean appIdConfigured = false;
        boolean apiKeyConfigured = false;
        boolean apiSecretConfigured = false;

        if ("baidu".equals(provider)) {
            AsrProperties.BaiduConfig baidu = asrProperties.getBaidu();
            if (baidu != null) {
                apiKeyConfigured = !isBlank(baidu.getApiKey());
                apiSecretConfigured = !isBlank(baidu.getSecretKey());
            }
        } else {
            // 默认讯飞
            AsrProperties.XunfeiConfig xfyun = asrProperties.getXfyun();
            if (xfyun != null) {
                appIdConfigured = !isBlank(xfyun.getAppId());
                apiKeyConfigured = !isBlank(xfyun.getApiKey());
                apiSecretConfigured = !isBlank(xfyun.getApiSecret());
            }
        }

        FfmpegCheck ffmpegCheck = checkFfmpeg(runtimeProperties.getFfmpegPath());

        AsrHealthResponse response = new AsrHealthResponse();
        response.setFfmpegAvailable(ffmpegCheck.available);
        response.setFfmpegPath(runtimeProperties.getFfmpegPath());
        response.setAppIdConfigured(appIdConfigured);
        response.setApiKeyConfigured(apiKeyConfigured);
        response.setApiSecretConfigured(apiSecretConfigured);

        if (!ffmpegCheck.available) {
            response.setMessage(ffmpegCheck.message);
        } else if (!appIdConfigured || (!apiKeyConfigured && !apiSecretConfigured)) {
            response.setMessage("缺少讯飞鉴权信息");
        } else {
            // 检查API密钥是否有效
            response.setApiTestResult("测试中...");
            response.setMessage("ok");
        }
        return response;
    }

    private FfmpegCheck checkFfmpeg(String ffmpegPath) {
        Process process = null;
        try {
            process = new ProcessBuilder(ffmpegPath, "-version")
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!finished) {
                process.destroyForcibly();
                return new FfmpegCheck(false, "ffmpeg 检测超时");
            }
            if (process.exitValue() != 0) {
                return new FfmpegCheck(false, "ffmpeg 无法执行: " + output);
            }
            return new FfmpegCheck(true, "ok");
        } catch (Exception ex) {
            return new FfmpegCheck(false, "未找到 ffmpeg 或无法执行");
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private ApiTestResult testApiConnectivity() {
        try {
            String provider = asrProperties.getProvider();
            boolean hasConfig = false;

            if ("baidu".equals(provider)) {
                AsrProperties.BaiduConfig baidu = asrProperties.getBaidu();
                hasConfig = baidu != null && !isBlank(baidu.getApiKey()) && !isBlank(baidu.getSecretKey());
            } else {
                AsrProperties.XunfeiConfig xfyun = asrProperties.getXfyun();
                hasConfig = xfyun != null && !isBlank(xfyun.getAppId()) && !isBlank(xfyun.getApiSecret());
            }

            if (!hasConfig) {
                return new ApiTestResult(false, "缺少" + provider + " API配置");
            }

            // 根据提供商进行不同的测试
            if ("baidu".equals(provider)) {
                return testBaiduConnectivity();
            } else {
                return testXunfeiConnectivity();
            }

        } catch (Exception e) {
            return new ApiTestResult(false, "测试失败: " + e.getMessage());
        }
    }

    private ApiTestResult testXunfeiConnectivity() throws Exception {
        AsrProperties.XunfeiConfig xfyun = asrProperties.getXfyun();
        if (xfyun == null) return new ApiTestResult(false, "未配置讯飞参数");

        String paramJson = objectMapper.writeValueAsString(Map.of(
                "engine_type", "sms16k", "aue", "raw", "auf", "audio/L16;rate=16000", "scene", "main"
        ));
        String paramBase64 = Base64.getEncoder().encodeToString(paramJson.getBytes(StandardCharsets.UTF_8));
        String curTime = String.valueOf(Instant.now().getEpochSecond());
        String checkSum = md5Hex(xfyun.getApiSecret() + curTime + paramBase64);

        String testBody = "audio=" + java.net.URLEncoder.encode(Base64.getEncoder().encodeToString(new byte[0]), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.xfyun.cn/v1/service/v1/iat"))
                .timeout(Duration.ofSeconds(10))
                .header("X-Appid", xfyun.getAppId())
                .header("X-CurTime", curTime)
                .header("X-Param", paramBase64)
                .header("X-CheckSum", checkSum)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(testBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            if (responseBody.contains("\"code\":\"0\"")) {
                return new ApiTestResult(true, "讯飞API验证成功");
            } else if (responseBody.contains("illegal access")) {
                return new ApiTestResult(false, "讯飞API密钥无效");
            } else {
                return new ApiTestResult(false, "讯飞API响应异常");
            }
        } else {
            return new ApiTestResult(false, "讯飞HTTP错误: " + response.statusCode());
        }
    }

    private ApiTestResult testBaiduConnectivity() throws Exception {
        AsrProperties.BaiduConfig baidu = asrProperties.getBaidu();
        if (baidu == null) return new ApiTestResult(false, "未配置百度参数");

        // 尝试获取Access Token来测试连接
        try {
            HttpClient client = HttpClient.newHttpClient();
            String params = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s",
                                        java.net.URLEncoder.encode(baidu.getApiKey(), StandardCharsets.UTF_8),
                                        java.net.URLEncoder.encode(baidu.getSecretKey(), StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://aip.baidubce.com/oauth/2.0/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                if (responseBody.contains("access_token")) {
                    return new ApiTestResult(true, "百度API验证成功");
                } else {
                    return new ApiTestResult(false, "百度API密钥无效");
                }
            } else {
                return new ApiTestResult(false, "百度HTTP错误: " + response.statusCode());
            }
        } catch (Exception e) {
            return new ApiTestResult(false, "百度网络连接失败: " + e.getMessage());
        }
    }

    private String md5Hex(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new RuntimeException("无法生成签名");
        }
    }

    private static class FfmpegCheck {
        private final boolean available;
        private final String message;

        private FfmpegCheck(boolean available, String message) {
            this.available = available;
            this.message = message;
        }
    }

    private static class ApiTestResult {
        private final boolean success;
        private final String message;

        private ApiTestResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}

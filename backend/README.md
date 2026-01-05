# 后端（Spring Boot）

## 快速开始

1) 安装 JDK 17 和 Maven。
2) 运行接口服务：

```bash
mvn spring-boot:run
```

## 接口基础路径

http://localhost:8080/api/v1

## 语音识别配置（讯飞）

方式一：环境变量（推荐）

- `XFYUN_APP_ID`
- `XFYUN_API_KEY`
- `XFYUN_API_SECRET`
- `FFMPEG_PATH`（可选，默认使用系统 PATH 中的 ffmpeg）

方式二：本地配置文件

复制 `src/main/resources/application-example.yml` 为 `src/main/resources/application-local.yml`，
并填写 `app-id / api-key / api-secret`。`application-local.yml` 已在 `.gitignore` 中忽略。

## 文案润色配置（硅基流动）

方式一：环境变量（推荐）

- `AI_POLISH_ENABLED`（true/false）
- `AI_POLISH_API_KEY`
- `AI_POLISH_MODEL`（示例：Qwen/Qwen2.5-7B-Instruct）
- `AI_POLISH_BASE_URL`（默认 https://api.siliconflow.cn）
- `AI_POLISH_CHAT_PATH`（默认 /v1/chat/completions）

方式二：本地配置文件

复制 `src/main/resources/application-example.yml` 为 `src/main/resources/application-local.yml`，
并填写 `ai.polish` 段落。`application-local.yml` 已在 `.gitignore` 中忽略。

## 说明

- 当前使用内存存储。
- 后续替换为数据库与真实 AI 服务。

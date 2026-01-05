# 情绪礼物（Music-Wise）

情绪礼物是一款“把祝福做成礼物”的创作型产品。发送者可以输入文字或语音，经过轻润色与情绪场景选择后，生成一份可分享的礼物链接；接收者打开后可看到祝福与场景，并反馈“听完/喜欢”。

## 主要功能

- 文字/语音输入（语音按住录制，转写为文字）
- AI 轻润色（当前为占位逻辑，便于联调）
- 情绪场景选择（warm / calm / power）
- 生成预览与可修改流程
- 礼物生成与接收页展示
- 接收端反馈（听完/喜欢）
- ASR 转写支持百度或讯飞（可切换）

## 目录结构

- `backend/`：Spring Boot 后端 API
- `web/`：H5 创作端与接收端
- `docs/`：产品与接口文档
- `miniprogram/`：小程序迁移预留

## 快速开始

### 1) 启动后端

```bash
cd backend
mvn spring-boot:run
```

接口基础路径：`http://localhost:8080/api/v1`

### 2) 启动前端

```bash
cd ..
python -m http.server 5173
```

浏览器访问：`http://localhost:5173/web/index.html`

## 配置说明（ASR）

复制配置模板：

```bash
cp backend/src/main/resources/application-example.yml backend/src/main/resources/application-local.yml
```

在 `application-local.yml` 中填写密钥（示例）：

```yaml
asr:
  provider: baidu  # 可选：baidu / xfyun
  ffmpeg-path: "ffmpeg"
  baidu:
    api-key: "your_api_key"
    secret-key: "your_secret_key"
  xfyun:
    app-id: "your_app_id"
    api-key: "your_api_key"
    api-secret: "your_api_secret"
```

说明：
- `ffmpeg-path` 可直接填 `ffmpeg`（已加入 PATH）或绝对路径。
- `application-local.yml` 应保留在本地，不提交仓库。

## 主要接口

- `POST /api/v1/polish`：文本润色（返回候选）
- `POST /api/v1/asr`：语音转文字（上传音频）
- `GET /api/v1/tracks`：获取情绪场景曲目
- `POST /api/v1/gifts`：生成礼物
- `GET /api/v1/gifts/{id}`：获取礼物详情
- `POST /api/v1/gifts/{id}/ack`：标记听完
- `POST /api/v1/gifts/{id}/like`：标记喜欢

## 开发提示

- 后端当前为内存存储，重启会丢失数据。
- 语音转写依赖 ffmpeg 转码为 PCM 16k。
- 真实 AI 润色与音乐匹配可在下一阶段接入。

## 安全注意

- 密钥不要提交到 GitHub。
- 若已提交过敏感信息，请及时删除并旋转密钥。

接口说明（版本 v1）

基础路径：/api/v1

返回格式统一为 JSON

1. 文案润色（模拟，含发送对象/祝福语）

POST /polish

请求

```json
{
  "content": "用户输入文本",
  "recipientType": "FAMILY | CLASSMATE | FRIEND | LOVER | OTHER | NONE",
  "blessingPreset": "BIRTHDAY | FESTIVAL | THANKS | CONGRATS | NONE",
  "mode": "text | voice"
}
```

响应

```json
{
  "combinedText": "拼接后的原始文案",
  "polishedText": "润色后的文案"
}
```

1.1 文案模板（开箱即用）

GET /polish/templates

响应

```json
[
  {
    "id": "T001",
    "title": "轻声感谢",
    "content": "谢谢你一直在我身边。很多话没说出口，但心里一直记得你的好。",
    "recipientType": "FRIEND",
    "blessingPreset": "THANKS",
    "tone": "温柔"
  }
]
```

2. 音乐检索（基于文案）

POST /tracks/search

请求

```json
{
  "text": "润色后的文案",
  "recipientType": "FAMILY | CLASSMATE | FRIEND | LOVER | OTHER | NONE",
  "blessingPreset": "BIRTHDAY | FESTIVAL | THANKS | CONGRATS | NONE"
}
```

响应

```json
{
  "vibe": "warm",
  "tracks": [
    {
      "id": 1,
      "url": "https://xxx/audio.mp3",
      "startAt": 12,
      "score": 0.82
    }
  ]
}
```

可选兜底：GET /tracks?vibe=warm

3. 创建情绪礼物

POST /gifts

请求

```json
{
  "content": "最终文案",
  "vibe": "warm",
  "mode": "text | voice"
}
```

响应

```json
{
  "giftId": 10001,
  "receiverPath": "/pages/receiver?giftId=10001"
}
```

4. 获取礼物详情（接收页）

GET /gifts/{giftId}

响应

```json
{
  "giftId": 10001,
  "content": "最终文案",
  "vibe": "warm",
  "trackUrl": "https://xxx/audio.mp3",
  "startAt": 12,
  "status": "CREATED | OPENED"
}
```

5. 礼物回执

POST /gifts/{giftId}/ack
POST /gifts/{giftId}/like

响应

```json
{
  "success": true
}
```

6. 语音转文字（语音听写）

POST /asr

请求（multipart/form-data）：

- file: 录音文件

响应：

```json
{
  "text": "这是语音转写示例内容。",
  "error": ""
}
```

7. 文字转语音（TTS）

POST /tts

请求

```json
{
  "content": "要合成的文案内容"
}
```

响应

```json
{
  "audioBase64": "base64编码音频",
  "audioType": "audio/mp3",
  "error": ""
}
```

8. 语音识别健康检查

GET /asr/health

响应示例：

```json
{
  "ffmpegAvailable": true,
  "ffmpegPath": "ffmpeg",
  "appIdConfigured": true,
  "apiKeyConfigured": true,
  "apiSecretConfigured": false,
  "message": "ok"
}
```

9. 枚举说明

RecipientType（发送对象）

- FAMILY 家人
- CLASSMATE 同学
- FRIEND 朋友
- LOVER 爱人
- OTHER 其他
- NONE 无

BlessingPreset（祝福语预设）

- BIRTHDAY 生日快乐
- FESTIVAL 节日快乐
- THANKS 感谢
- CONGRATS 祝贺
- NONE 无

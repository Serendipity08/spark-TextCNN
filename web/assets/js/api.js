const DEFAULT_BASE = "http://localhost:8080/api/v1";
const API_BASE = window.__API_BASE__ || DEFAULT_BASE;

async function request(path, options = {}) {
  const config = { ...options };
  const isFormData = typeof FormData !== "undefined" && config.body instanceof FormData;
  if (!isFormData) {
    config.headers = {
      "Content-Type": "application/json",
      ...(options.headers || {})
    };
  } else {
    config.headers = {
      ...(options.headers || {})
    };
  }

  const response = await fetch(`${API_BASE}${path}`, config);
  if (!response.ok) {
    throw new Error(`请求失败：${response.status}`);
  }
  return response.json();
}

export function polishText(payload) {
  return request("/polish", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function getTracks(vibe) {
  return request(`/tracks?vibe=${encodeURIComponent(vibe)}`);
}

export function createGift(content, vibe, mode, music = undefined) {
  const payload = { content, vibe, mode };
  if (music) {
    payload.musicId = music.id || "";
    payload.musicTitle = music.title || "";
    payload.musicArtist = music.artist || "";
    payload.musicPreviewUrl = music.previewUrl || "";
    payload.musicStartAt = typeof music.startAt === "number" ? music.startAt : undefined;
  }
  return request("/gifts", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function getGift(giftId) {
  return request(`/gifts/${encodeURIComponent(giftId)}`);
}

export function ackGift(giftId) {
  return request(`/gifts/${encodeURIComponent(giftId)}/ack`, {
    method: "POST"
  });
}

export function likeGift(giftId) {
  return request(`/gifts/${encodeURIComponent(giftId)}/like`, {
    method: "POST"
  });
}

export function transcribeAudio(blob, filename = "recording.webm") {
  const formData = new FormData();
  formData.append("file", blob, filename);
  return request("/asr", {
    method: "POST",
    body: formData
  });
}

export function getPolishTemplates() {
  return request("/polish/templates");
}

export function synthesizeTts(content) {
  return request("/tts", {
    method: "POST",
    body: JSON.stringify({ content })
  });
}

export function searchMusic(query, limit = 3) {
  const params = new URLSearchParams({
    q: query,
    limit: String(limit)
  });
  return request(`/music/search?${params.toString()}`);
}

export function mixAudio(ttsBlob, { musicUrl = "", musicBlob = null } = {}, options = {}) {
  const formData = new FormData();
  formData.append("tts", ttsBlob, "tts.wav");
  if (musicBlob) {
    formData.append("music", musicBlob, "music.mp3");
  }
  if (musicUrl) {
    formData.append("musicUrl", musicUrl);
  }
  if (options.musicVolume) {
    formData.append("musicVolume", String(options.musicVolume));
  }
  if (options.targetDuration) {
    formData.append("targetDuration", String(options.targetDuration));
  }
  return request("/mix", {
    method: "POST",
    body: formData
  });
}

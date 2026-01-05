import { polishText, createGift, getTracks, transcribeAudio, getPolishTemplates, synthesizeTts, searchMusic } from "./api.js?v=20260106";
import { $, showToast, formatTime } from "./utils.js";
import { FALLBACK_TEMPLATES, buildHints } from "./templates.js?v=20260106";

let currentMode = "text";
let selectedVibe = "warm";
let finalMessage = "";
let isRecording = false;
let timerInterval = null;
let recordSeconds = 0;
let previewVisible = false;
let mediaRecorder = null;
let audioChunks = [];
let recordingStream = null;
let recordStartAt = 0;
let recordingMimeType = "";
let templates = [];
let ttsContent = "";
let ttsLoading = false;
let ttsObjectUrl = "";
let musicResults = [];
let musicSelected = null;
let musicPreviewingId = null;
const musicAudio = new Audio();
musicAudio.crossOrigin = "anonymous";
const previewMusicAudio = new Audio();
previewMusicAudio.crossOrigin = "anonymous";
let previewMusicUrl = "";
let previewStartAt = 0;
let previewMusicDuration = 0;

const textInput = $("#text-content");
const candidatesEl = $("#candidates");
const resultEl = $("#result");
const giftLinkEl = $("#gift-link");
const previewEl = $("#preview");
const previewMessageEl = $("#preview-message");
const previewSceneEl = $("#preview-scene");
const previewStartEl = $("#preview-start");
const recordBtn = $("#record-btn");
const recordLabel = recordBtn ? recordBtn.querySelector(".record-label") : null;
const recordTimer = $("#record-timer");
const recipientSelect = $("#recipient-type");
const blessingSelect = $("#blessing-preset");
const templateListEl = $("#template-list");
const templateEmptyEl = $("#template-empty");
const hintsEl = $("#input-hints");
const polishStatusEl = $("#polish-status");
const ttsGenerateBtn = $("#tts-generate");
const ttsStatusEl = $("#tts-status");
const ttsPlayerEl = $("#tts-player");
const ttsPlayBtn = $("#tts-play");
const ttsPlayLabel = ttsPlayBtn ? ttsPlayBtn.querySelector(".tts-play-label") : null;
const ttsBarEl = $("#tts-bar");
const ttsProgressEl = $("#tts-progress");
const ttsTimeEl = $("#tts-time");
const ttsAudioEl = $("#tts-audio");
const musicQueryInput = $("#music-query");
const musicResultsEl = $("#music-results");
const musicSelectedEl = $("#music-selected");

const vibeLabels = {
  warm: "温暖",
  calm: "安静",
  power: "有力量"
};

const recipientLabels = {
  FAMILY: "家人",
  CLASSMATE: "同学",
  FRIEND: "朋友",
  LOVER: "爱人",
  OTHER: "其他",
  NONE: "不限"
};

const blessingLabels = {
  BIRTHDAY: "生日快乐",
  FESTIVAL: "节日快乐",
  THANKS: "感谢",
  CONGRATS: "祝贺",
  NONE: "不限"
};

const trackCache = {};
const itunesPreviewCache = {};

musicAudio.addEventListener("error", () => {
  showToast("音频加载失败");
  musicPreviewingId = null;
  renderMusicResults(musicResults);
});

musicAudio.addEventListener("ended", () => {
  musicPreviewingId = null;
  renderMusicResults(musicResults);
});
previewMusicAudio.addEventListener("error", () => {
  showToast("背景音乐无法播放");
});
previewMusicAudio.addEventListener("loadedmetadata", () => {
  previewMusicDuration = Number.isFinite(previewMusicAudio.duration) ? previewMusicAudio.duration : 0;
  updateTtsProgress();
});
previewMusicAudio.addEventListener("timeupdate", updateTtsProgress);

function resetMusicPreview() {
  if (musicAudio) {
    musicAudio.pause();
    musicAudio.currentTime = 0;
  }
  musicPreviewingId = null;
}

function resetPreviewMusic() {
  previewMusicAudio.pause();
  previewMusicAudio.currentTime = 0;
  previewMusicDuration = 0;
}

function setPreviewMusic(url, startAt) {
  previewMusicUrl = url || "";
  previewStartAt = Number.isFinite(startAt) ? startAt : 0;
  resetPreviewMusic();
}

async function fetchItunesPreview(item) {
  const artist = (item.artist || "").trim();
  const title = (item.title || "").trim();
  const query = `${artist} ${title}`.trim();
  if (!query) {
    return null;
  }
  const cacheKey = query.toLowerCase();
  if (Object.prototype.hasOwnProperty.call(itunesPreviewCache, cacheKey)) {
    return itunesPreviewCache[cacheKey];
  }
  const searchUrl = `https://itunes.apple.com/search?term=${encodeURIComponent(query)}&media=music&limit=1`;
  try {
    const res = await fetch(searchUrl);
    if (!res.ok) {
      throw new Error("itunes search failed");
    }
    const data = await res.json();
    const preview = data && data.results && data.results.length ? data.results[0].previewUrl || null : null;
    itunesPreviewCache[cacheKey] = preview;
    return preview;
  } catch (error) {
    itunesPreviewCache[cacheKey] = null;
    return null;
  }
}

async function resolveSelectedMusicPreview(item) {
  if (!item) {
    return "";
  }
  if (item._effectivePreviewUrl) {
    return item._effectivePreviewUrl;
  }
  // 先尝试 iTunes 预览，与试听一致
  const itunesPreview = await fetchItunesPreview(item);
  if (itunesPreview) {
    item._effectivePreviewUrl = itunesPreview;
    return itunesPreview;
  }
  if (item.previewUrl) {
    item._effectivePreviewUrl = item.previewUrl;
    return item.previewUrl;
  }
  return "";
}

function renderMusicResults(results) {
  if (!musicResultsEl) {
    return;
  }
  musicResultsEl.innerHTML = "";
  if (!results || !results.length) {
    const empty = document.createElement("div");
    empty.className = "music-empty";
    empty.textContent = "未找到匹配的歌曲，试试其他关键词";
    musicResultsEl.appendChild(empty);
    return;
  }

  results.slice(0, 3).forEach(item => {
    const row = document.createElement("div");
    row.className = "music-item";
    if (musicSelected && musicSelected.id === item.id) {
      row.classList.add("selected");
    }

    const info = document.createElement("div");
    info.className = "music-info";

    const title = document.createElement("div");
    title.className = "music-title";
    title.textContent = `${item.artist || "未知歌手"} - ${item.title || "未命名歌曲"}`;

    const meta = document.createElement("div");
    meta.className = "music-meta";
    meta.textContent = item.previewUrl ? "可试听" : "无预览音频";

    info.appendChild(title);
    info.appendChild(meta);

    const actions = document.createElement("div");
    actions.className = "music-actions";

    const previewBtn = document.createElement("button");
    previewBtn.type = "button";
    previewBtn.className = "ghost-btn";
    previewBtn.textContent = musicPreviewingId === item.id ? "暂停" : "试听";
    previewBtn.addEventListener("click", async () => {
      if (musicPreviewingId === item.id && !musicAudio.paused) {
        resetMusicPreview();
        previewBtn.textContent = "试听";
        return;
      }
      resetMusicPreview();
      previewBtn.textContent = "加载中...";
      previewBtn.disabled = true;
      try {
        const itunesPreview = await fetchItunesPreview(item);
        const previewUrl = itunesPreview || item.previewUrl;
        if (!previewUrl) {
          showToast("未找到可试听音频");
          previewBtn.textContent = "试听";
          return;
        }
        musicAudio.src = previewUrl;
        musicAudio.load();
        musicAudio.play().catch(() => showToast("播放失败"));
        item._effectivePreviewUrl = previewUrl;
        musicPreviewingId = item.id;
        document.querySelectorAll(".music-actions .ghost-btn").forEach(btn => {
          if (btn !== previewBtn && btn.textContent === "暂停") {
            btn.textContent = "试听";
          }
        });
        previewBtn.textContent = "暂停";
      } finally {
        previewBtn.disabled = false;
      }
    });

    const selectBtn = document.createElement("button");
    selectBtn.type = "button";
    selectBtn.className = "ghost-btn";
    selectBtn.textContent = "选择";
    selectBtn.addEventListener("click", async () => {
      selectBtn.disabled = true;
      selectBtn.textContent = "设置中...";
      try {
        musicSelected = item;
        // 主动做一次 iTunes 预览查找，保证选择逻辑与试听一致
        const itunesPreview = await fetchItunesPreview(item);
        if (itunesPreview) {
          item._effectivePreviewUrl = itunesPreview;
        }
        const selectedPreview = await resolveSelectedMusicPreview(item);
        const targetUrl = selectedPreview || item._effectivePreviewUrl || item.previewUrl;
        setPreviewMusic(targetUrl, 0);
        if (musicSelectedEl) {
          musicSelectedEl.textContent = `已选择：${item.artist || "未知"} - ${item.title || "未命名"}`;
          musicSelectedEl.classList.remove("hidden");
        }
        renderMusicResults(musicResults);
        showToast("已将音乐同步到预览");
      } finally {
        selectBtn.disabled = false;
        selectBtn.textContent = "选择";
      }
    });

    actions.appendChild(previewBtn);
    actions.appendChild(selectBtn);

    row.appendChild(info);
    row.appendChild(actions);

    musicResultsEl.appendChild(row);
  });
}

async function handleMusicSearch() {
  if (!musicQueryInput || !musicResultsEl) {
    return;
  }
  const q = musicQueryInput.value.trim();
  if (!q) {
    showToast("请输入音乐风格或关键词");
    return;
  }
  musicResultsEl.innerHTML = '<div class="music-empty">正在搜索...</div>';
  resetMusicPreview();
  try {
    const response = await searchMusic(q, 3);
    musicResults = response.tracks || [];
    renderMusicResults(musicResults);
  } catch (error) {
    musicResultsEl.innerHTML = '<div class="music-empty">搜索失败，请稍后再试</div>';
  }
}

function normalizeTemplates(response) {
  if (!response) {
    return [];
  }
  const list = Array.isArray(response) ? response : (response.templates || []);
  return list.filter(item => item && item.content);
}

async function loadTemplates() {
  try {
    const response = await getPolishTemplates();
    templates = normalizeTemplates(response);
  } catch (error) {
    templates = FALLBACK_TEMPLATES;
  }
  if (!templates.length) {
    templates = FALLBACK_TEMPLATES;
  }
  renderTemplates();
}

function matchTemplate(item, recipientType, blessingPreset) {
  if (!item) {
    return false;
  }
  const targetRecipient = recipientType || "NONE";
  const targetBlessing = blessingPreset || "NONE";
  return item.recipientType === targetRecipient && item.blessingPreset === targetBlessing;
}

function filterTemplates(list, recipientType, blessingPreset) {
  if (!list || !list.length) {
    return [];
  }
  return list.filter(item => matchTemplate(item, recipientType, blessingPreset));
}

function renderTemplates() {
  if (!templateListEl) {
    return;
  }
  const recipientType = getRecipientType();
  const blessingPreset = getBlessingPreset();
  const list = filterTemplates(templates, recipientType, blessingPreset);
  templateListEl.innerHTML = "";
  if (!list.length) {
    if (templateEmptyEl) {
      templateEmptyEl.classList.remove("hidden");
    }
    return;
  }
  if (templateEmptyEl) {
    templateEmptyEl.classList.add("hidden");
  }
  list.forEach(item => {
    const card = document.createElement("button");
    card.type = "button";
    card.className = "template-card";
    const title = document.createElement("div");
    title.className = "template-title";
    title.textContent = item.title || "模板";
    const content = document.createElement("div");
    content.className = "template-content";
    content.textContent = item.content;
    card.appendChild(title);
    card.appendChild(content);
    const tags = [];
    if (item.recipientType && item.recipientType !== "NONE") {
      tags.push(recipientLabels[item.recipientType] || item.recipientType);
    }
    if (item.blessingPreset && item.blessingPreset !== "NONE") {
      tags.push(blessingLabels[item.blessingPreset] || item.blessingPreset);
    }
    if (tags.length) {
      const tagEl = document.createElement("div");
      tagEl.className = "template-tags";
      tagEl.textContent = tags.join(" · ");
      card.appendChild(tagEl);
    }
    card.addEventListener("click", () => applyTemplate(item));
    templateListEl.appendChild(card);
  });
}

function applyTemplate(item) {
  if (!item) {
    return;
  }
  currentMode = "text";
  if (item.recipientType && item.recipientType !== "NONE") {
    recipientSelect.value = item.recipientType;
  }
  if (item.blessingPreset && item.blessingPreset !== "NONE") {
    blessingSelect.value = item.blessingPreset;
  }
  textInput.value = item.content;
  finalMessage = "";
  renderTemplates();
  updateHints();
  updatePolishStatus();
  resetTtsPlayer();
  showToast("已填入模板");
  if (previewVisible) {
    updatePreview(false);
  }
}


function pickExampleTemplate() {
  const recipientType = getRecipientType();
  const blessingPreset = getBlessingPreset();
  const list = filterTemplates(templates, recipientType, blessingPreset);
  if (!list.length) {
    return null;
  }
  const index = Math.floor(Math.random() * list.length);
  return list[index];
}

function buildLocalFallback(content, recipientType, blessingPreset) {
  if (content) {
    return content;
  }
  const list = filterTemplates(templates, recipientType, blessingPreset);
  if (list.length) {
    return list[0].content;
  }
  return "送上你的心意。";
}

function updateHints() {
  if (!hintsEl) {
    return;
  }
  const content = getContent().trim();
  if (content) {
    hintsEl.innerHTML = "";
    hintsEl.classList.add("hidden");
    return;
  }
  const hints = buildHints(getRecipientType(), getBlessingPreset());
  hintsEl.innerHTML = "";
  if (!hints.length) {
    hintsEl.classList.add("hidden");
    return;
  }
  hintsEl.classList.remove("hidden");
  hints.forEach(text => {
    const tag = document.createElement("span");
    tag.className = "input-hint";
    tag.textContent = text;
    hintsEl.appendChild(tag);
  });
}

function updatePolishStatus() {
  if (!polishStatusEl) {
    return;
  }
  const content = getContent().trim();
  if (currentMode === "voice") {
    const ready = Boolean(content);
    polishStatusEl.textContent = ready ? "已满足润色条件，可以润色" : "请先说点什么";
    polishStatusEl.classList.toggle("ready", ready);
    return;
  }
  const filledCount = countPolishInputs(content, getRecipientType(), getBlessingPreset());
  if (filledCount < 2) {
    polishStatusEl.textContent = `还差 ${2 - filledCount} 项即可润色`;
    polishStatusEl.classList.remove("ready");
  } else {
    polishStatusEl.textContent = "已满足润色条件，可以润色";
    polishStatusEl.classList.add("ready");
  }
}

function normalizeAudioType(type) {
  if (!type) {
    return "audio/mpeg";
  }
  if (type.toLowerCase() === "audio/mp3") {
    return "audio/mpeg";
  }
  return type;
}

function base64ToBytes(base64) {
  const sanitized = base64.replace(/\s/g, "");
  const binary = atob(sanitized);
  const length = binary.length;
  const bytes = new Uint8Array(length);
  for (let i = 0; i < length; i += 1) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

function setTtsStatus(message, isError) {
  if (!ttsStatusEl) {
    return;
  }
  ttsStatusEl.textContent = message;
  ttsStatusEl.classList.toggle("error", Boolean(isError));
}

function updateTtsPlayState(isPlaying) {
  if (ttsPlayBtn) {
    ttsPlayBtn.classList.toggle("playing", isPlaying);
  }
  if (ttsPlayLabel) {
    ttsPlayLabel.textContent = isPlaying ? "暂停" : "播放";
  }
}

function startBackgroundMusicForPreview() {
  if (!previewMusicUrl) {
    return;
  }
  if (previewMusicAudio.src !== previewMusicUrl) {
    previewMusicAudio.src = previewMusicUrl;
    previewMusicAudio.load();
  }
  const tryPlay = () => {
    console.log("[preview music] url:", previewMusicUrl, "audio.src:", previewMusicAudio.src);
    try {
      previewMusicAudio.currentTime = previewStartAt;
    } catch (error) {
      // ignore seek errors before metadata is ready
    }
    previewMusicAudio.play().catch(() => {
      showToast("背景音乐无法播放");
    });
  };

  if (previewMusicAudio.paused) {
    tryPlay();
  }
}

function pauseBackgroundMusicForPreview() {
  if (!previewMusicAudio.paused) {
    previewMusicAudio.pause();
  }
}

function resetTtsPlayer() {
  ttsContent = "";
  if (ttsObjectUrl) {
    URL.revokeObjectURL(ttsObjectUrl);
    ttsObjectUrl = "";
  }
  if (ttsAudioEl) {
    ttsAudioEl.pause();
    ttsAudioEl.removeAttribute("src");
    ttsAudioEl.load();
  }
  if (ttsPlayerEl) {
    ttsPlayerEl.classList.add("hidden");
  }
  if (ttsProgressEl) {
    ttsProgressEl.style.width = "0%";
  }
  if (ttsTimeEl) {
    ttsTimeEl.textContent = "00:00 / 00:00";
  }
  resetPreviewMusic();
  updateTtsPlayState(false);
  setTtsStatus("生成语音后可试听", false);
}

function updateTtsProgress() {
  if (!ttsAudioEl) {
    return;
  }
  const ttsDuration = Number.isFinite(ttsAudioEl.duration) ? ttsAudioEl.duration : 0;
  const musicDuration = Number.isFinite(previewMusicDuration) ? previewMusicDuration : 0;
  const duration = Math.max(ttsDuration, musicDuration);
  const current = Math.max(ttsAudioEl.currentTime || 0, previewMusicAudio.currentTime || 0);
  if (ttsProgressEl) {
    const percent = duration ? (current / duration) * 100 : 0;
    ttsProgressEl.style.width = `${percent}%`;
  }
  if (ttsTimeEl) {
    ttsTimeEl.textContent = `${formatTime(current)} / ${formatTime(duration)}`;
  }
}

async function handleTtsGenerate() {
  const content = resolveContent();
  if (!content) {
    showToast("请先输入内容");
    return;
  }
  if (ttsLoading) {
    return;
  }
  if (ttsContent === content && ttsAudioEl && ttsAudioEl.src) {
    ttsAudioEl.currentTime = 0;
    ttsAudioEl.play().catch(() => {});
    return;
  }
  ttsLoading = true;
  if (ttsGenerateBtn) {
    ttsGenerateBtn.disabled = true;
    ttsGenerateBtn.textContent = "生成中...";
  }
  setTtsStatus("正在生成语音...", false);
  try {
    const response = await synthesizeTts(content);
    if (response.error) {
      throw new Error(response.error);
    }
    if (!response.audioBase64) {
      throw new Error("未返回音频");
    }
    const audioType = normalizeAudioType(response.audioType);
    const audioBytes = base64ToBytes(response.audioBase64);
    const audioBlob = new Blob([audioBytes], { type: audioType });
    if (ttsObjectUrl) {
      URL.revokeObjectURL(ttsObjectUrl);
    }
    ttsObjectUrl = URL.createObjectURL(audioBlob);
    if (ttsAudioEl) {
      ttsAudioEl.src = ttsObjectUrl;
      ttsAudioEl.currentTime = 0;
      ttsAudioEl.muted = false;
      ttsAudioEl.volume = 1;
      ttsAudioEl.load();
    }
    ttsContent = content;
    if (ttsPlayerEl) {
      ttsPlayerEl.classList.remove("hidden");
    }
    updateTtsProgress();
    setTtsStatus("语音已生成，可播放预览", false);

    // 合成背景音乐
    showToast("语音生成完成");
  } catch (error) {
    const message = error && error.message ? error.message : "语音生成失败";
    setTtsStatus(`生成失败：${message}`, true);
    showToast("语音生成失败");
  } finally {
    ttsLoading = false;
    if (ttsGenerateBtn) {
      ttsGenerateBtn.disabled = false;
      ttsGenerateBtn.textContent = "生成语音";
    }
  }
}

function handleTtsPlay() {
  if (!ttsAudioEl || !ttsAudioEl.src) {
    showToast("请先生成语音");
    return;
  }
  if (ttsAudioEl.paused) {
    startBackgroundMusicForPreview();
    ttsAudioEl.play().catch(() => {});
  } else {
    pauseBackgroundMusicForPreview();
    ttsAudioEl.pause();
  }
}

function handleTtsSeek(event) {
  if (!ttsAudioEl || !ttsBarEl || !ttsAudioEl.duration) {
    return;
  }
  const rect = ttsBarEl.getBoundingClientRect();
  const offset = Math.min(Math.max(0, event.clientX - rect.left), rect.width);
  ttsAudioEl.currentTime = (offset / rect.width) * ttsAudioEl.duration;
}

function startTimer() {
  recordSeconds = 0;
  updateTimer();
  timerInterval = setInterval(() => {
    recordSeconds += 1;
    updateTimer();
  }, 1000);
}

function stopTimer() {
  clearInterval(timerInterval);
  timerInterval = null;
}

function updateTimer() {
  const minutes = Math.floor(recordSeconds / 60).toString().padStart(2, "0");
  const seconds = (recordSeconds % 60).toString().padStart(2, "0");
  recordTimer.textContent = `${minutes}:${seconds}`;
}

function setRecordingUI(active) {
  recordBtn.classList.toggle("recording", active);
  const label = active ? "松开结束" : "按住说话";
  if (recordLabel) {
    recordLabel.textContent = label;
  } else {
    recordBtn.textContent = label;
  }
  recordBtn.setAttribute("aria-pressed", active ? "true" : "false");
  if (recordTimer) {
    recordTimer.classList.toggle("active", active);
  }
}

function renderCandidates(candidates) {
  candidatesEl.innerHTML = "";
  finalMessage = "";
  if (!candidates || !candidates.length) {
    return;
  }
  candidates.forEach((text, index) => {
    const card = document.createElement("div");
    card.className = "candidate";
    card.textContent = text;
    card.addEventListener("click", () => {
      document.querySelectorAll(".candidate").forEach(el => el.classList.remove("selected"));
      card.classList.add("selected");
      finalMessage = text;
      textInput.value = text;
      currentMode = "text";
      updateHints();
      resetTtsPlayer();

      showToast("已选择润色版本");
      if (previewVisible) {
        updatePreview(false);
      }
    });
    if (index === 0) {
      card.classList.add("selected");
      finalMessage = text;
    }
    candidatesEl.appendChild(card);
  });
  updatePolishStatus();
}

function getContent() {
  return textInput.value;
}

function getRecipientType() {
  return recipientSelect ? recipientSelect.value : "NONE";
}

function getBlessingPreset() {
  return blessingSelect ? blessingSelect.value : "NONE";
}

function countPolishInputs(content, recipientType, blessingPreset) {
  let count = 0;
  if (content) {
    count += 1;
  }
  if (recipientType && recipientType !== "NONE") {
    count += 1;
  }
  if (blessingPreset && blessingPreset !== "NONE") {
    count += 1;
  }
  return count;
}

function resolveContent() {
  const raw = finalMessage || getContent();
  return raw.trim();
}

function setExample() {
  currentMode = "text";
  const template = pickExampleTemplate();
  if (template) {
    textInput.value = template.content;
  } else {
    textInput.value = "愿你在新的日子里，心中有光，脚下有路。";
  }
  finalMessage = "";
  updateHints();
  updatePolishStatus();
  resetTtsPlayer();
  if (previewVisible) {
    updatePreview(false);
  }
}


async function startRecording(event) {
  if (event) {
    event.preventDefault();
  }
  if (isRecording) {
    return;
  }
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia || typeof MediaRecorder === "undefined") {
    showToast("当前浏览器不支持录音");
    return;
  }
  try {
    recordingStream = await navigator.mediaDevices.getUserMedia({ audio: true });
    const supportedTypes = [
      "audio/webm;codecs=opus",
      "audio/webm",
      "audio/ogg;codecs=opus"
    ];
    recordingMimeType = supportedTypes.find(type => MediaRecorder.isTypeSupported(type)) || "";
    mediaRecorder = recordingMimeType
      ? new MediaRecorder(recordingStream, { mimeType: recordingMimeType })
      : new MediaRecorder(recordingStream);
    audioChunks = [];
    mediaRecorder.ondataavailable = eventData => {
      if (eventData.data && eventData.data.size > 0) {
        audioChunks.push(eventData.data);
      }
    };
    mediaRecorder.onstop = handleRecordingStop;
    mediaRecorder.onerror = () => {
      showToast("录音发生错误");
    };
    mediaRecorder.start(1000);
    isRecording = true;
    recordStartAt = Date.now();
    setRecordingUI(true);
    startTimer();
  } catch (error) {
    showToast("无法获取麦克风权限");
  }
}

function stopRecording(event) {
  if (event) {
    event.preventDefault();
  }
  if (!isRecording || !mediaRecorder) {
    return;
  }
  mediaRecorder.stop();
}

async function handleRecordingStop() {
  stopTimer();
  setRecordingUI(false);
  isRecording = false;

  if (recordingStream) {
    recordingStream.getTracks().forEach(track => track.stop());
    recordingStream = null;
  }

  const durationMs = Date.now() - recordStartAt;
  const type = mediaRecorder && mediaRecorder.mimeType ? mediaRecorder.mimeType : "audio/webm";
  const blob = new Blob(audioChunks, { type });
  if (durationMs < 800 || !blob || blob.size < 2048) {
    showToast("录音时间太短，请重新录制");
    return;
  }

  showToast("正在识别...");
  try {
    const extension = type.includes("ogg") ? "ogg" : "webm";
    const response = await transcribeAudio(blob, `recording.${extension}`);
    if (response.error) {
      showToast(response.error);
      return;
    }
    const text = response.text ? response.text.trim() : "";
    textInput.value = text || "未识别到内容";
    currentMode = "voice";
    finalMessage = "";
    updateHints();
    updatePolishStatus();
    resetTtsPlayer();
    showToast("语音转写完成");
    if (previewVisible) {
      updatePreview(false);
    }
  } catch (error) {
    showToast("语音识别失败");
  }
}

async function handlePolish() {
  const content = getContent().trim();
  const recipientType = getRecipientType();
  const blessingPreset = getBlessingPreset();
  if (currentMode === "text") {
    const filledCount = countPolishInputs(content, recipientType, blessingPreset);
    if (filledCount < 2) {
      showToast("发送对象、祝福语、内容需至少填写两项");
      return;
    }
  } else if (!content) {
    showToast("请先输入内容");
    return;
  }
  try {
    const response = await polishText({
      content,
      mode: currentMode,
      recipientType,
      blessingPreset
    });
    const candidates = response.polishedText
      ? [response.polishedText]
      : (response.candidates || []);
    renderCandidates(candidates);
    showToast("润色完成");
  } catch (error) {
    const fallbackText = buildLocalFallback(content, recipientType, blessingPreset);
    renderCandidates([fallbackText]);
    showToast("已生成本地润色");
  }
}

async function getTrackForVibe(vibe) {
  if (trackCache[vibe]) {
    return trackCache[vibe];
  }
  try {
    const response = await getTracks(vibe);
    const track = response.tracks && response.tracks.length ? response.tracks[0] : null;
    trackCache[vibe] = track;
    return track;
  } catch (error) {
    return null;
  }
}

async function updatePreview(showToastOnEmpty = true) {
  const content = resolveContent();
  if (!content) {
    if (showToastOnEmpty) {
      showToast("请先输入内容");
    }
    return false;
  }
  if (ttsContent !== content) {
    resetTtsPlayer();
  }
  previewMessageEl.textContent = content;
  const track = await getTrackForVibe(selectedVibe);
  const selectedPreview = musicSelected ? await resolveSelectedMusicPreview(musicSelected) : "";
  const musicUrl = selectedPreview
    || (musicSelected && musicSelected._effectivePreviewUrl)
    || previewMusicUrl;
  if (!musicUrl) {
    showToast("请选择或试听一首可播放的音乐");
    return false;
  }
  setPreviewMusic(musicUrl, 0);
  previewSceneEl.textContent = `场景：${vibeLabels[selectedVibe] || selectedVibe}`;
  previewStartEl.textContent = "试听片段";
  previewEl.classList.remove("hidden");
  resultEl.classList.add("hidden");
  previewVisible = true;
  return true;
}

async function handleGenerate() {
  await updatePreview(true);
}

async function handleConfirmGenerate() {
  const content = resolveContent();
  if (!content) {
    showToast("请先输入内容");
    return;
  }
  const musicPayload = musicSelected ? { ...musicSelected } : undefined;
  if (musicPayload) {
    delete musicPayload.startAt;
    delete musicPayload.musicStartAt;
  }
  try {
    const response = await createGift(content, selectedVibe, currentMode, musicPayload);
    const base = window.location.origin + window.location.pathname.replace("index.html", "receiver.html");
    const link = `${base}?giftId=${response.giftId}`;
    giftLinkEl.textContent = link;
    resultEl.classList.remove("hidden");
    showToast("礼物已生成");
  } catch (error) {
    showToast("生成礼物失败");
  }
}

function copyLink() {
  const link = giftLinkEl.textContent;
  if (!link) {
    return;
  }
  navigator.clipboard.writeText(link).then(() => {
    showToast("链接已复制");
  });
}

function openReceiver() {
  const link = giftLinkEl.textContent;
  if (link) {
    window.open(link, "_blank");
  }
}

// 事件绑定

$("#example-btn").addEventListener("click", setExample);

if (recipientSelect) {
  recipientSelect.addEventListener("change", () => {
    finalMessage = "";
    renderTemplates();
    updateHints();
    updatePolishStatus();
  });
}

if (blessingSelect) {
  blessingSelect.addEventListener("change", () => {
    finalMessage = "";
    renderTemplates();
    updateHints();
    updatePolishStatus();
  });
}
recordBtn.addEventListener("pointerdown", startRecording);
recordBtn.addEventListener("pointerup", stopRecording);
recordBtn.addEventListener("pointerleave", stopRecording);
recordBtn.addEventListener("pointercancel", stopRecording);
$("#polish-btn").addEventListener("click", handlePolish);
$("#generate-btn").addEventListener("click", handleGenerate);
$("#refresh-preview").addEventListener("click", () => updatePreview(true));
$("#change-scene").addEventListener("click", () => {
  const scenes = document.querySelector(".scenes");
  if (scenes) {
    scenes.scrollIntoView({ behavior: "smooth", block: "center" });
  }
  showToast("可选择新的场景");
});
$("#re-polish").addEventListener("click", handlePolish);
$("#confirm-generate").addEventListener("click", handleConfirmGenerate);
$("#copy-link").addEventListener("click", copyLink);
$("#open-receiver").addEventListener("click", openReceiver);
$("#music-search-btn").addEventListener("click", handleMusicSearch);
if (musicQueryInput) {
  musicQueryInput.addEventListener("keydown", event => {
    if (event.key === "Enter") {
      handleMusicSearch();
    }
  });
}
if (ttsGenerateBtn) {
  ttsGenerateBtn.addEventListener("click", handleTtsGenerate);
}
if (ttsPlayBtn) {
  ttsPlayBtn.addEventListener("click", handleTtsPlay);
}
if (ttsBarEl) {
  ttsBarEl.addEventListener("click", handleTtsSeek);
}
if (ttsAudioEl) {
  ttsAudioEl.addEventListener("timeupdate", updateTtsProgress);
  ttsAudioEl.addEventListener("loadedmetadata", updateTtsProgress);
  ttsAudioEl.addEventListener("error", () => {
    setTtsStatus("音频播放失败", true);
    showToast("音频无法播放");
  });
  ttsAudioEl.addEventListener("ended", () => {
    updateTtsPlayState(false);
    resetPreviewMusic();
  });
  ttsAudioEl.addEventListener("play", () => {
    startBackgroundMusicForPreview();
    updateTtsPlayState(true);
  });
  ttsAudioEl.addEventListener("pause", () => {
    updateTtsPlayState(false);
    pauseBackgroundMusicForPreview();
  });
}

textInput.addEventListener("input", () => {
  finalMessage = "";
  currentMode = "text";
  resetTtsPlayer();
  if (previewVisible) {
    updatePreview(false);
  }
  updateHints();
  updatePolishStatus();
});

document.querySelectorAll(".scene").forEach(scene => {
  scene.addEventListener("click", () => {
    document.querySelectorAll(".scene").forEach(el => el.classList.remove("selected"));
    scene.classList.add("selected");
    selectedVibe = scene.dataset.vibe;
    if (previewVisible) {
      updatePreview(false);
    }
  });
});

loadTemplates();
updateHints();
updatePolishStatus();
resetTtsPlayer();

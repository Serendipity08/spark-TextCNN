import { getGift, ackGift, likeGift } from "./api.js";
import { $, showToast, formatTime, getQueryParam } from "./utils.js";

const giftId = getQueryParam("giftId");
const audioEl = $("#gift-audio");
const musicTrackEl = $("#music-track");
let totalDuration = 0;
let startAtSeconds = 0;
audioEl.crossOrigin = "anonymous";

audioEl.addEventListener("error", () => {
  showToast("音频加载失败");
});

async function loadGift() {
  if (!giftId) {
    showError();
    return;
  }
  try {
    const gift = await getGift(giftId);
    $("#message").textContent = gift.content;
    $("#scene-badge").textContent = gift.vibe;
    $("#start-at").textContent = `从高潮前 ${gift.startAt} 秒开始`;
    startAtSeconds = gift.startAt || 0;

    const title = `${gift.musicArtist || ""} - ${gift.musicTitle || ""}`.trim();
    musicTrackEl.textContent = title || "背景音乐";

    if (gift.trackUrl) {
      audioEl.src = gift.trackUrl;
    }

    attachAudioEvents();
    $("#loading").classList.add("hidden");
    $("#content").classList.remove("hidden");
  } catch (error) {
    showError();
  }
}

function attachAudioEvents() {
  audioEl.addEventListener("loadedmetadata", () => {
    totalDuration = audioEl.duration || 0;
    $("#total-time").textContent = formatTime(totalDuration || 0);
    if (startAtSeconds > 0 && startAtSeconds < (audioEl.duration || 0)) {
      audioEl.currentTime = startAtSeconds;
    }
  });
  audioEl.addEventListener("timeupdate", () => {
    const current = audioEl.currentTime || 0;
    $("#current-time").textContent = formatTime(current);
    const duration = audioEl.duration || totalDuration || 1;
    const progress = Math.min(100, (current / duration) * 100);
    $("#progress-bar").style.width = `${progress}%`;
  });
  audioEl.addEventListener("ended", () => {
    showToast("播放完成");
  });
}

function showError() {
  $("#loading").classList.add("hidden");
  $("#error").classList.remove("hidden");
}

function startPlayback() {
  const overlay = $("#overlay");
  overlay.classList.add("hidden");
  $("#player").classList.remove("hidden");
  audioEl.play().catch(() => {
    showToast("无法播放音频");
  });
}

async function handleAck() {
  if (!giftId) {
    return;
  }
  try {
    await ackGift(giftId);
    showToast("已收到反馈");
  } catch (error) {
    showToast("反馈发送失败");
  }
}

async function handleLike() {
  if (!giftId) {
    return;
  }
  try {
    await likeGift(giftId);
    showToast("已收到反馈");
  } catch (error) {
    showToast("反馈发送失败");
  }
}

$("#overlay").addEventListener("click", startPlayback);
$("#ack-btn").addEventListener("click", handleAck);
$("#like-btn").addEventListener("click", handleLike);

loadGift();

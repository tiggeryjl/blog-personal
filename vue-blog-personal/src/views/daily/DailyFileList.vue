<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { Document } from '@element-plus/icons-vue';

const props = defineProps({
  files: {
    type: Array,
    default: () => [],
  },
});

const VIDEO_EXTENSIONS = new Set(['mp4', 'webm', 'ogg', 'ogv', 'mov', 'm4v']);

const videoModalVisible = ref(false);
const activeVideo = ref(null);
const videoPlayer = ref(null);
let previousBodyOverflow = '';

const getFileExtension = (url) => {
  try {
    const pathname = new URL(String(url), window.location.origin).pathname;
    const filename = decodeURIComponent(pathname.split('/').filter(Boolean).pop() || '');
    return filename.includes('.') ? filename.split('.').pop().toLowerCase() : '';
  } catch {
    const path = String(url).split(/[?#]/)[0];
    const filename = path.split('/').filter(Boolean).pop() || '';
    return filename.includes('.') ? filename.split('.').pop().toLowerCase() : '';
  }
};

const isVideoFile = (file) => VIDEO_EXTENSIONS.has(getFileExtension(file?.url));

const videoFiles = computed(() => props.files.filter(isVideoFile));
const attachmentFiles = computed(() => props.files.filter((file) => !isVideoFile(file)));

const getVideoPreviewUrl = (url) => `${url}${String(url).includes('#') ? '' : '#t=0.1'}`;

const openVideo = async (file) => {
  activeVideo.value = file;
  videoModalVisible.value = true;
  previousBodyOverflow = document.body.style.overflow;
  document.body.style.overflow = 'hidden';
  await nextTick();
  videoPlayer.value?.play().catch(() => {});
};

const closeVideo = () => {
  if (videoPlayer.value) {
    videoPlayer.value.pause();
  }
  videoModalVisible.value = false;
  activeVideo.value = null;
  document.body.style.overflow = previousBodyOverflow;
};

const handleKeydown = (event) => {
  if (event.key === 'Escape' && videoModalVisible.value) {
    closeVideo();
  }
};

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown);
  if (videoModalVisible.value) {
    document.body.style.overflow = previousBodyOverflow;
  }
});
</script>

<template>
  <div v-if="files.length" class="card-files">
    <div v-if="videoFiles.length" class="video-grid">
      <button
        v-for="(file, idx) in videoFiles"
        :key="`video-${idx}-${file.url}`"
        type="button"
        class="video-card"
        :aria-label="`播放视频：${file.name}`"
        @click="openVideo(file)"
      >
        <video
          :src="getVideoPreviewUrl(file.url)"
          class="video-card-frame"
          preload="auto"
          muted
          playsinline
          webkit-playsinline
          x5-playsinline
          disablepictureinpicture
          disableremoteplayback
          tabindex="-1"
          aria-hidden="true"
        ></video>
        <span class="video-card-shade"></span>
        <span class="video-play" aria-hidden="true">
          <span class="daily-video-triangle"></span>
        </span>
        <span class="video-caption">
          <span class="video-name">{{ file.name }}</span>
          <span class="video-label">视频</span>
        </span>
      </button>
    </div>

    <div v-if="attachmentFiles.length" class="attachment-list">
      <el-link
        v-for="(file, idx) in attachmentFiles"
        :key="`file-${idx}-${file.url}`"
        :href="file.url"
        target="_blank"
        rel="noopener noreferrer"
        type="primary"
        class="file-link"
        :underline="false"
      >
        <el-icon class="file-icon"><Document /></el-icon>
        <span class="file-name">{{ file.name }}</span>
      </el-link>
    </div>

    <Teleport to="body">
      <Transition name="video-modal">
        <div
          v-if="videoModalVisible"
          class="daily-video-modal"
          role="dialog"
          aria-modal="true"
          :aria-label="`视频播放：${activeVideo?.name || ''}`"
          @click.self="closeVideo"
        >
          <button type="button" class="video-modal-close" aria-label="关闭视频" @click="closeVideo">
            ×
          </button>

          <div class="video-modal-panel">
            <div class="video-modal-header">
              <span class="video-modal-title">{{ activeVideo?.name }}</span>
              <span class="video-modal-tip">支持进度、音量、倍速和全屏操作</span>
            </div>
            <video
              v-if="activeVideo"
              ref="videoPlayer"
              :src="activeVideo.url"
              class="video-modal-player"
              controls
              autoplay
              playsinline
              preload="auto"
            ></video>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.card-files {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  min-width: 0;
  padding-top: 12px;
  border-top: 1px solid rgba(127, 127, 127, 0.24);
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 360px));
  gap: 12px;
  width: 100%;
}

.video-card {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  min-width: 0;
  padding: 0;
  overflow: hidden;
  border: 1px solid rgba(127, 127, 127, 0.24);
  border-radius: 12px;
  background: #10151d;
  color: #fff;
  cursor: pointer;
  isolation: isolate;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.video-card:hover,
.video-card:focus-visible {
  transform: translateY(-2px);
  border-color: var(--primary-color);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.2);
  outline: none;
}

.video-card-frame {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
}

.video-card-frame::-webkit-media-controls,
.video-card-frame::-webkit-media-controls-enclosure,
.video-card-frame::-webkit-media-controls-panel,
.video-card-frame::-webkit-media-controls-start-playback-button {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
  opacity: 0 !important;
  pointer-events: none !important;
  -webkit-appearance: none !important;
}

.video-card-shade {
  position: absolute;
  inset: 0;
  z-index: 1;
  background:
    linear-gradient(to bottom, rgba(5, 10, 18, 0.08), rgba(5, 10, 18, 0.62)),
    radial-gradient(circle at center, transparent 10%, rgba(5, 10, 18, 0.16) 70%);
}

.video-play {
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  transform: translate(-50%, -50%);
  filter: drop-shadow(0 4px 10px rgba(0, 0, 0, 0.58));
  transition: transform 0.2s ease;
}

.daily-video-triangle {
  width: 0;
  height: 0;
  border-top: 17px solid transparent;
  border-bottom: 17px solid transparent;
  border-left: 27px solid rgba(255, 255, 255, 0.96);
  transform: translateX(4px);
}

.video-card:hover .video-play,
.video-card:focus-visible .video-play {
  transform: translate(-50%, -50%) scale(1.08);
}

.video-caption {
  position: absolute;
  right: 12px;
  bottom: 10px;
  left: 12px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-width: 0;
  text-align: left;
}

.video-name {
  min-width: 0;
  overflow: hidden;
  font-size: 13px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-label {
  flex: 0 0 auto;
  padding: 2px 7px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 11px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.file-link {
  align-self: flex-start;
  width: fit-content;
  max-width: 100%;
  min-width: 0;
  justify-content: flex-start;
  line-height: 1.5;
}

.file-link :deep(.el-link__inner) {
  align-items: flex-start;
  gap: 6px;
  max-width: 100%;
  text-align: left;
}

.file-icon {
  flex: 0 0 auto;
  margin-top: 0.2em;
}

.file-name {
  min-width: 0;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.file-link:hover .file-name,
.file-link:focus-visible .file-name {
  text-decoration: underline;
  text-decoration-thickness: 1px;
  text-underline-offset: 3px;
}

.daily-video-modal {
  position: fixed;
  inset: 0;
  z-index: 10050;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: rgba(4, 8, 14, 0.9);
  backdrop-filter: blur(7px);
}

.video-modal-panel {
  width: min(1120px, 92vw);
  max-height: 92vh;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 16px;
  background: #0b0f15;
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.5);
}

.video-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 14px 18px;
  color: #fff;
  background: rgba(255, 255, 255, 0.035);
}

.video-modal-title {
  min-width: 0;
  overflow: hidden;
  font-size: 15px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-modal-tip {
  flex: 0 0 auto;
  color: rgba(255, 255, 255, 0.56);
  font-size: 12px;
}

.video-modal-player {
  display: block;
  width: 100%;
  max-height: calc(92vh - 58px);
  background: #000;
}

.video-modal-close {
  position: fixed;
  top: 22px;
  right: 28px;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition:
    background 0.2s ease,
    transform 0.2s ease;
}

.video-modal-close:hover,
.video-modal-close:focus-visible {
  background: rgba(255, 255, 255, 0.22);
  outline: none;
  transform: rotate(90deg);
}

.video-modal-enter-active,
.video-modal-leave-active {
  transition: opacity 0.2s ease;
}

.video-modal-enter-from,
.video-modal-leave-to {
  opacity: 0;
}

@media (max-width: 600px) {
  .video-grid {
    grid-template-columns: 1fr;
  }

  .video-play {
    width: 62px;
    height: 62px;
  }

  .daily-video-modal {
    padding: 12px;
  }

  .video-modal-panel {
    width: 100%;
    border-radius: 12px;
  }

  .video-modal-header {
    padding: 12px 14px;
  }

  .video-modal-tip {
    display: none;
  }

  .video-modal-close {
    top: 6px;
    right: 6px;
    width: 38px;
    height: 38px;
  }
}
</style>

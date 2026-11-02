<script setup>
import { computed, nextTick, ref } from 'vue';
import { Document } from '@element-plus/icons-vue';

const props = defineProps({
  files: {
    type: [String, Array],
    default: '',
  },
  compact: {
    type: Boolean,
    default: false,
  },
});

const VIDEO_EXTENSIONS = new Set(['mp4', 'webm', 'ogg', 'ogv', 'mov', 'm4v']);

const videoDialogVisible = ref(false);
const activeVideo = ref(null);
const videoPlayer = ref(null);

const getFileNameFromUrl = (url) => {
  try {
    const decoded = decodeURIComponent(String(url).split(/[?#]/)[0]);
    return decoded.split('/').filter(Boolean).pop() || '附件';
  } catch {
    return String(url).split(/[?#]/)[0].split('/').filter(Boolean).pop() || '附件';
  }
};

const getFileExtension = (url) => {
  const name = getFileNameFromUrl(url);
  return name.includes('.') ? name.split('.').pop().toLowerCase() : '';
};

const normalizedFiles = computed(() => {
  const source = Array.isArray(props.files)
    ? props.files
    : typeof props.files === 'string'
      ? props.files.split(',')
      : [];

  return source
    .map((file) => {
      const url = typeof file === 'string' ? file.trim() : file?.url;
      if (!url) return null;
      return {
        url,
        name: getFileNameFromUrl(url),
        isVideo: VIDEO_EXTENSIONS.has(getFileExtension(url)),
      };
    })
    .filter(Boolean);
});

const visibleFiles = computed(() => (props.compact ? normalizedFiles.value.slice(0, 2) : normalizedFiles.value));
const hiddenCount = computed(() => Math.max(0, normalizedFiles.value.length - visibleFiles.value.length));

const getVideoPreviewUrl = (url) => `${url}${String(url).includes('#') ? '' : '#t=0.1'}`;

const openVideo = async (file) => {
  activeVideo.value = file;
  videoDialogVisible.value = true;
  await nextTick();
  videoPlayer.value?.play().catch(() => {});
};

const stopVideo = () => {
  if (videoPlayer.value) {
    videoPlayer.value.pause();
  }
  activeVideo.value = null;
};
</script>

<template>
  <div v-if="normalizedFiles.length" class="daily-attachment-preview" :class="{ compact }">
    <template v-for="file in visibleFiles" :key="file.url">
      <button
        v-if="file.isVideo"
        type="button"
        class="daily-video-card"
        :aria-label="`播放视频：${file.name}`"
        @click.stop="openVideo(file)"
      >
        <video
          :src="getVideoPreviewUrl(file.url)"
          class="daily-video-thumb"
          preload="auto"
          muted
          playsinline
          webkit-playsinline
          disablepictureinpicture
          disableremoteplayback
          tabindex="-1"
          aria-hidden="true"
        ></video>
        <span class="daily-video-shade"></span>
        <span class="daily-video-play" aria-hidden="true">
          <span class="daily-video-triangle"></span>
        </span>
        <span class="daily-video-name">{{ file.name }}</span>
      </button>

      <el-link
        v-else
        :href="file.url"
        target="_blank"
        rel="noopener noreferrer"
        type="primary"
        class="daily-attachment-link"
        :underline="false"
      >
        <el-icon><Document /></el-icon>
        <span>{{ file.name }}</span>
      </el-link>
    </template>

    <span v-if="hiddenCount" class="daily-file-more">+{{ hiddenCount }}</span>

    <el-dialog
      v-model="videoDialogVisible"
      :title="activeVideo?.name || '视频预览'"
      width="min(960px, 92vw)"
      top="7vh"
      append-to-body
      destroy-on-close
      class="daily-video-dialog"
      @closed="stopVideo"
    >
      <video
        v-if="activeVideo"
        ref="videoPlayer"
        :src="activeVideo.url"
        class="daily-video-player"
        controls
        autoplay
        playsinline
        preload="auto"
      ></video>
    </el-dialog>
  </div>
</template>

<style scoped>
.daily-attachment-preview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 320px));
  gap: 10px;
  width: 100%;
  min-width: 0;
}

.daily-attachment-preview.compact {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 5px;
}

.daily-video-card {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  min-width: 0;
  padding: 0;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #111827;
  color: #fff;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.daily-video-card:hover,
.daily-video-card:focus-visible {
  border-color: var(--el-color-primary);
  outline: none;
  box-shadow: 0 5px 16px rgba(31, 45, 61, 0.18);
}

.daily-video-thumb {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
}

.daily-video-thumb::-webkit-media-controls,
.daily-video-thumb::-webkit-media-controls-enclosure,
.daily-video-thumb::-webkit-media-controls-panel,
.daily-video-thumb::-webkit-media-controls-start-playback-button {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
  opacity: 0 !important;
  pointer-events: none !important;
  -webkit-appearance: none !important;
}

.daily-video-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 38%, rgba(0, 0, 0, 0.72));
}

.daily-video-play {
  position: absolute;
  top: 50%;
  left: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  filter: drop-shadow(0 3px 8px rgba(0, 0, 0, 0.55));
  transform: translate(-50%, -50%);
}

.daily-video-triangle {
  width: 0;
  height: 0;
  border-top: 14px solid transparent;
  border-bottom: 14px solid transparent;
  border-left: 23px solid rgba(255, 255, 255, 0.96);
  transform: translateX(3px);
}

.daily-video-name {
  position: absolute;
  right: 9px;
  bottom: 7px;
  left: 9px;
  overflow: hidden;
  color: #fff;
  font-size: 12px;
  line-height: 1.4;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.compact .daily-video-card {
  width: 138px;
  max-width: 100%;
  margin: 0 auto;
  border-radius: 7px;
}

.compact .daily-video-play {
  width: 46px;
  height: 46px;
}

.compact .daily-video-triangle {
  border-top-width: 10px;
  border-bottom-width: 10px;
  border-left-width: 17px;
}

.compact .daily-video-name {
  right: 6px;
  bottom: 4px;
  left: 6px;
  font-size: 10px;
}

.daily-attachment-link {
  display: inline-flex;
  max-width: 100%;
  line-height: 1.5;
  text-align: left;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.daily-attachment-link :deep(.el-link__inner) {
  display: inline-flex;
  align-items: flex-start;
  gap: 5px;
  min-width: 0;
  text-align: left;
}

.compact .daily-attachment-link {
  justify-content: center;
  font-size: 12px;
}

.daily-file-more {
  color: #909399;
  font-size: 12px;
  text-align: center;
}

.daily-video-player {
  display: block;
  width: 100%;
  max-height: 76vh;
  background: #000;
}
</style>

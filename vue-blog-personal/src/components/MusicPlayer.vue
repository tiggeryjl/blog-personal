<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getMusicListApi } from '@/api/music'

const LOOP_MODE_KEY = 'blog_music_loop_mode'
const VOLUME_KEY = 'blog_music_volume'
const DEFAULT_VOLUME = 70

const getStoredNumber = (key, fallback, min, max) => {
  const value = Number(localStorage.getItem(key))
  return Number.isFinite(value) && value >= min && value <= max ? value : fallback
}

const audioEl = ref(null)
const musicContainer = ref(null)
const tracks = ref([])
const currentIndex = ref(0)
const isPlaying = ref(false)
const showPanel = ref(false)
const showVolume = ref(false)
const currentTime = ref(0)
const audioDuration = ref(0)
const volume = ref(getStoredNumber(VOLUME_KEY, DEFAULT_VOLUME, 0, 100))
const loopMode = ref(getStoredNumber(LOOP_MODE_KEY, 0, 0, 2))
const lyricLines = ref([])

const currentSong = computed(() => tracks.value[currentIndex.value] || null)
const progress = computed(() => {
  const total = audioDuration.value || Number(currentSong.value?.duration) || 0
  return total ? Math.min(100, (currentTime.value / total) * 100) : 0
})
const displayDuration = computed(
  () => audioDuration.value || Number(currentSong.value?.duration) || 0
)
const loopIcon = computed(() => {
  if (loopMode.value === 1) return ['fas', 'arrow-rotate-right']
  if (loopMode.value === 2) return ['fas', 'shuffle']
  return ['fas', 'repeat']
})
const loopText = computed(() => {
  if (loopMode.value === 1) return '单曲循环'
  if (loopMode.value === 2) return '随机播放'
  return '列表循环'
})
const volumeIcon = computed(() => {
  if (volume.value === 0) return ['fas', 'volume-xmark']
  if (volume.value < 45) return ['fas', 'volume-low']
  return ['fas', 'volume-high']
})
const activeLyricIndex = computed(() => {
  if (!lyricLines.value.length) return -1
  let index = -1
  for (let i = 0; i < lyricLines.value.length; i += 1) {
    if (lyricLines.value[i].time <= currentTime.value + 0.15) index = i
    else break
  }
  return index
})

const formatTime = (seconds) => {
  const value = Number(seconds) || 0
  const minutes = Math.floor(value / 60)
  const rest = Math.floor(value % 60)
  return `${String(minutes).padStart(2, '0')}:${String(rest).padStart(2, '0')}`
}

const parseLrc = (text) => {
  const result = []
  String(text || '')
    .split(/\r?\n/)
    .forEach((line) => {
      const timeTags = [...line.matchAll(/\[(\d{1,2}):(\d{2}(?:\.\d{1,3})?)\]/g)]
      if (!timeTags.length) return
      const content = line.replace(/\[[^\]]+\]/g, '').trim()
      if (!content) return
      timeTags.forEach((tag) => {
        result.push({
          time: Number(tag[1]) * 60 + Number(tag[2]),
          text: content,
        })
      })
    })
  return result.sort((a, b) => a.time - b.time)
}

let lyricRequestVersion = 0
const loadLyrics = async (song) => {
  const requestVersion = ++lyricRequestVersion
  lyricLines.value = []
  if (!song?.lyric) return

  try {
    let content = song.lyric
    if (/^https?:\/\//i.test(song.lyric)) {
      const response = await fetch(song.lyric)
      if (!response.ok) throw new Error('歌词请求失败')
      content = await response.text()
    }
    if (requestVersion === lyricRequestVersion) {
      lyricLines.value = parseLrc(content)
    }
  } catch (error) {
    console.warn('加载歌词失败：', error)
  }
}

const playAudio = async () => {
  if (!audioEl.value || !currentSong.value?.url) return
  try {
    await audioEl.value.play()
  } catch (error) {
    if (error?.name === 'NotAllowedError') {
      ElMessage.info('浏览器限制自动播放，请点击播放按钮开始')
    } else {
      console.warn('播放失败：', error)
    }
  }
}

const togglePlay = async () => {
  if (!audioEl.value || !currentSong.value) return
  if (isPlaying.value) {
    audioEl.value.pause()
    return
  }
  await playAudio()
}

const randomIndex = () => {
  if (tracks.value.length <= 1) return 0
  let index = currentIndex.value
  while (index === currentIndex.value) {
    index = Math.floor(Math.random() * tracks.value.length)
  }
  return index
}

const switchSong = async (index, shouldPlay = true) => {
  if (!tracks.value.length) return
  currentIndex.value = (index + tracks.value.length) % tracks.value.length
  currentTime.value = 0
  audioDuration.value = 0
  await nextTick()
  audioEl.value?.load()
  loadLyrics(currentSong.value)
  if (shouldPlay) await playAudio()
}

const prevSong = () => {
  if (loopMode.value === 2) {
    switchSong(randomIndex())
    return
  }
  switchSong(currentIndex.value - 1)
}

const nextSong = () => {
  if (loopMode.value === 2) {
    switchSong(randomIndex())
    return
  }
  switchSong(currentIndex.value + 1)
}

const handleEnded = () => {
  if (loopMode.value === 1) {
    audioEl.value.currentTime = 0
    playAudio()
    return
  }
  nextSong()
}

const handleTimeUpdate = () => {
  if (!audioEl.value) return
  currentTime.value = audioEl.value.currentTime || 0
}

const handleLoadedMetadata = () => {
  audioDuration.value = Number(audioEl.value?.duration) || 0
}

const handlePlay = () => {
  isPlaying.value = true
}

const handlePause = () => {
  isPlaying.value = false
}

let errorNoticeLocked = false
const handleAudioError = () => {
  if (errorNoticeLocked || !currentSong.value) return
  errorNoticeLocked = true
  ElMessage.warning(`《${currentSong.value.name}》播放失败，已尝试切换下一首`)
  window.setTimeout(() => {
    errorNoticeLocked = false
  }, 1600)
  if (tracks.value.length > 1) nextSong()
}

const seekAudio = (event) => {
  if (!audioEl.value || !displayDuration.value) return
  const percent = Number(event.target.value)
  audioEl.value.currentTime = (percent / 100) * displayDuration.value
  currentTime.value = audioEl.value.currentTime
}

const changeVolume = (event) => {
  volume.value = Number(event.target.value)
}

const toggleMute = () => {
  volume.value = volume.value > 0 ? 0 : DEFAULT_VOLUME
}

const changeLoopMode = () => {
  loopMode.value = (loopMode.value + 1) % 3
}

const toggleShowPanel = () => {
  if (!tracks.value.length) return
  showPanel.value = !showPanel.value
  if (!showPanel.value) showVolume.value = false
}

const clickOutside = (event) => {
  if (musicContainer.value && !musicContainer.value.contains(event.target)) {
    showPanel.value = false
    showVolume.value = false
  }
}

watch(loopMode, (value) => localStorage.setItem(LOOP_MODE_KEY, String(value)))
watch(volume, (value) => {
  localStorage.setItem(VOLUME_KEY, String(value))
  if (audioEl.value) audioEl.value.volume = value / 100
})

const loadTracks = async () => {
  try {
    const result = await getMusicListApi()
    if (result.code !== 200 || !Array.isArray(result.data)) return
    tracks.value = result.data.filter((item) => item?.url)
    currentIndex.value = 0
    if (tracks.value.length) {
      await nextTick()
      if (audioEl.value) audioEl.value.volume = volume.value / 100
      loadLyrics(currentSong.value)
    }
  } catch (error) {
    console.warn('获取音乐列表失败：', error)
  }
}

onMounted(() => {
  loadTracks()
  window.addEventListener('click', clickOutside)
})

onUnmounted(() => {
  window.removeEventListener('click', clickOutside)
  audioEl.value?.pause()
})
</script>

<template>
  <div v-if="tracks.length" ref="musicContainer" class="music-container">
    <audio
      ref="audioEl"
      :src="currentSong?.url"
      preload="metadata"
      @timeupdate="handleTimeUpdate"
      @loadedmetadata="handleLoadedMetadata"
      @ended="handleEnded"
      @play="handlePlay"
      @pause="handlePause"
      @error="handleAudioError"
    ></audio>

    <div
      class="music-mini"
      :class="{ 'is-open': showPanel }"
      role="button"
      :aria-expanded="showPanel"
      aria-label="打开音乐播放器"
      @click="toggleShowPanel"
    >
      <div class="music-cover" :class="{ playing: isPlaying }">
        <img v-if="currentSong?.cover" :src="currentSong.cover" alt="歌曲封面" />
        <font-awesome-icon v-else :icon="['fas', 'music']" />
      </div>

      <div class="music-right">
        <div class="music-track-title">
          <span class="music-title-main">
            <span class="music-equalizer" :class="{ playing: isPlaying }" aria-hidden="true">
              <i></i><i></i><i></i>
            </span>
            <span class="music-label">{{ currentSong?.name }}</span>
          </span>
          <span class="music-singer">{{ currentSong?.singer || '未知歌手' }}</span>
        </div>
        <div class="music-btns">
          <button class="mini-control" type="button" title="上一首" @click.stop="prevSong">
            <font-awesome-icon :icon="['fas', 'backward-step']" />
          </button>
          <button class="mini-control" type="button" :title="isPlaying ? '暂停' : '播放'" @click.stop="togglePlay">
            <font-awesome-icon :icon="isPlaying ? ['fas', 'pause'] : ['fas', 'play']" />
          </button>
          <button class="mini-control" type="button" title="下一首" @click.stop="nextSong">
            <font-awesome-icon :icon="['fas', 'forward-step']" />
          </button>
        </div>
        <div class="progress-bar-mini">
          <div class="progress" :style="{ width: progress + '%' }"></div>
        </div>
      </div>
    </div>

    <transition name="music-pop">
      <div v-show="showPanel" class="music-panel" @click.stop>
        <div
          v-if="currentSong?.cover"
          class="panel-backdrop"
          :style="{ backgroundImage: `url('${currentSong.cover}')` }"
        ></div>
        <div class="panel-glow"></div>

        <div class="panel-kicker">
          <span class="kicker-now">
            <i :class="{ playing: isPlaying }"></i>
            NOW PLAYING
          </span>
          <span>{{ String(currentIndex + 1).padStart(2, '0') }} / {{ String(tracks.length).padStart(2, '0') }}</span>
        </div>

        <div class="panel-head">
          <div class="cover-big" :class="{ playing: isPlaying }">
            <span class="cover-orbit"></span>
            <img v-if="currentSong?.cover" :src="currentSong.cover" alt="歌曲封面" />
            <font-awesome-icon v-else :icon="['fas', 'music']" />
          </div>
          <div class="song-info">
            <span class="song-name">{{ currentSong?.name }}</span>
            <span class="singer">{{ currentSong?.singer || '未知歌手' }}</span>
            <span class="track-count">共 {{ tracks.length }} 首</span>
          </div>
          <span class="mode-label">{{ loopText }}</span>
        </div>

        <div v-if="lyricLines.length" class="lyric-box">
          <div
            v-for="(line, index) in lyricLines"
            :key="`${line.time}-${index}`"
            class="lyric-line"
            :class="{ active: activeLyricIndex === index }"
          >
            {{ line.text }}
          </div>
        </div>
        <div v-else class="lyric-empty">
          <font-awesome-icon :icon="['fas', 'wave-square']" />
          <span>纯音乐，请闭上眼睛欣赏</span>
        </div>

        <div class="time-row">
          <span>{{ formatTime(currentTime) }}</span>
          <span>{{ formatTime(displayDuration) }}</span>
        </div>
        <input
          class="progress-range"
          type="range"
          min="0"
          max="100"
          step="0.1"
          :value="progress"
          :style="{ '--progress': `${progress}%` }"
          aria-label="播放进度"
          @input="seekAudio"
        />

        <div class="panel-controls">
          <button class="control-btn" type="button" :title="loopText" @click="changeLoopMode">
            <font-awesome-icon :icon="loopIcon" />
          </button>
          <button class="control-btn" type="button" title="上一首" @click="prevSong">
            <font-awesome-icon :icon="['fas', 'backward-step']" />
          </button>
          <button class="control-btn" type="button" :title="isPlaying ? '暂停' : '播放'" @click="togglePlay">
            <font-awesome-icon :icon="isPlaying ? ['fas', 'pause'] : ['fas', 'play']" />
          </button>
          <button class="control-btn" type="button" title="下一首" @click="nextSong">
            <font-awesome-icon :icon="['fas', 'forward-step']" />
          </button>
          <div class="volume-wrap">
            <button class="control-btn" type="button" title="音量" @click="showVolume = !showVolume">
              <font-awesome-icon :icon="volumeIcon" />
            </button>
            <div v-if="showVolume" class="volume-popover">
              <button type="button" title="静音" @click="toggleMute">
                <font-awesome-icon :icon="volumeIcon" />
              </button>
              <input
                type="range"
                min="0"
                max="100"
                :value="volume"
                :style="{ '--progress': `${volume}%` }"
                aria-label="音量"
                @input="changeVolume"
              />
            </div>
          </div>
        </div>

        <div class="playlist-head">
          <span>
            <font-awesome-icon :icon="['fas', 'list-ul']" />
            播放列表
          </span>
          <span>{{ tracks.length }} TRACKS</span>
        </div>
        <div class="playlist">
          <button
            v-for="(item, index) in tracks"
            :key="item.id || `${item.url}-${index}`"
            class="playlist-item"
            :class="{ active: currentIndex === index }"
            type="button"
            @click="switchSong(index)"
          >
            <span class="num">
              <span v-if="currentIndex === index && isPlaying" class="playing-bars" aria-label="正在播放">
                <i></i><i></i><i></i>
              </span>
              <template v-else>{{ String(index + 1).padStart(2, '0') }}</template>
            </span>
            <span class="item-cover">
              <img v-if="item.cover" :src="item.cover" alt="" />
              <font-awesome-icon v-else :icon="['fas', 'music']" />
            </span>
            <span class="item-info">
              <span class="name">{{ item.name }}</span>
              <span class="singer">{{ item.singer || '未知歌手' }}</span>
            </span>
            <span class="item-duration">{{ formatTime(item.duration) }}</span>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>


<style scoped>
.music-container {
  position: relative;
  display: flex;
  align-items: center;
  height: 100%;
  overflow: visible;
}

.music-mini {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  height: 100%;
  padding: 0 8px;
}

.music-cover {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.music-cover img {
  /* 去掉行内图片底部基线空隙，避免圆形封面里多出 6px 无用高度 */
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.music-right {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  width: 120px;
  flex-shrink: 0;
}

.music-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  width: 100%;
}

.music-label {
  font-size: 14px;
  color: var(--text-color);
  margin-right: 4px;
}

.progress-bar-mini {
  width: 100%;
  height: 3px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  cursor: pointer;
}

.progress-bar-mini .progress {
  height: 100%;
  background: var(--primary-color);
  border-radius: 3px;
  transition: width 0.1s;
}

.music-panel {
  position: absolute;
  top: 100%;
  left: 0;
  width: 380px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 12px;
  z-index: 99999 !important;
}

.panel-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.cover-big {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
}

.cover-big img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-info {
  display: flex;
  flex-direction: column;
}

.song-name {
  font-size: 14px;
  font-weight: bold;
}

.singer {
  font-size: 12px;
  color: #666;
}

.lyric {
  flex: 1;
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.panel-controls {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-bottom: 8px;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: #eee;
  border-radius: 2px;
  cursor: pointer;
  margin-bottom: 10px;
}

.progress-bar .progress {
  height: 100%;
  background: #409eff;
  transition: width 0.1s;
}

.divider {
  height: 1px;
  background: #eee;
  margin: 8px 0;
}

.playlist {
  max-height: 220px;
  overflow-y: auto;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  cursor: pointer;
  border-radius: 4px;
}

.playlist-item:hover {
  background: #f5f5f5;
}

.playlist-item.active {
  background: #e8f3ff;
  color: #409eff;
}

.num {
  width: 20px;
  text-align: center;
  font-size: 12px;
  color: #666;
}

.item-cover {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  overflow: hidden;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-info .name {
  font-size: 13px;
}

.item-info .singer {
  font-size: 11px;
  color: #999;
}

.add-music {
  text-align: center;
  padding: 8px 0;
}

/* 后台音乐库播放器 */
.music-container > audio {
  display: none;
}

.music-container button {
  border: 0;
  font: inherit;
}

.music-mini {
  gap: 9px;
  padding: 0 2px;
  border-radius: 22px;
  transition: background-color 0.22s ease;
}

.music-cover {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #9fd8ff, #4f91ed);
  box-shadow: 0 5px 16px rgba(79, 145, 237, 0.2);
}

.music-cover.playing img {
  animation: cover-spin 14s linear infinite;
}

.music-cover.playing {
  box-shadow: 0 0 0 3px rgba(235, 247, 255, 0.62), 0 6px 18px rgba(79, 145, 237, 0.24);
}

.music-right {
  width: 145px;
  gap: 3px;
}

.music-track-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
}

.music-track-title .music-label {
  margin: 0;
  max-width: 92px;
  overflow: hidden;
  color: var(--text-color);
  font-size: 12px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.music-singer {
  max-width: 48px;
  overflow: hidden;
  color: var(--text-color);
  font-size: 10px;
  opacity: 0.56;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.music-btns {
  justify-content: center;
  gap: 9px;
}

.mini-control {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  color: var(--text-color);
  background: transparent;
  cursor: pointer;
  opacity: 0.62;
  transition: color 0.2s ease, opacity 0.2s ease, transform 0.2s ease;
}

.mini-control {
  width: 16px;
  height: 16px;
  font-size: 10px;
}

.mini-control:hover {
  opacity: 1;
  transform: scale(1.08);
}

.progress-bar-mini {
  height: 2px;
  overflow: hidden;
  background: color-mix(in srgb, var(--text-color) 15%, transparent);
}

.progress-bar-mini .progress {
  background: linear-gradient(90deg, #b6e2ff, #5b9df3);
}

.music-panel {
  top: calc(100% + 11px);
  left: 50%;
  width: 390px;
  padding: 18px;
  overflow: hidden;
  color: #233047;
  background:
    radial-gradient(circle at 100% 0, rgba(157, 213, 255, 0.38), transparent 34%),
    rgba(244, 250, 255, 0.97);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 20px;
  box-shadow: 0 22px 60px rgba(30, 41, 69, 0.2);
  backdrop-filter: blur(22px);
  transform: translateX(-50%);
  transform-origin: top center;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 13px;
  min-width: 0;
}

.cover-big {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  flex: 0 0 58px;
  color: #fff;
  background: linear-gradient(145deg, #a9ddff, #4c8eee);
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(76, 142, 238, 0.23);
}

.cover-big img {
  border-radius: inherit;
}

.panel-head .song-info {
  min-width: 0;
  flex: 1;
  gap: 3px;
}

.panel-head .song-name {
  overflow: hidden;
  color: #1d293d;
  font-size: 15px;
  font-weight: 750;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.panel-head .singer {
  color: #758096;
}

.track-count {
  color: #a1a9b8;
  font-size: 10px;
}

.mode-label {
  align-self: flex-start;
  padding: 4px 8px;
  color: #4384db;
  background: #edf7ff;
  border-radius: 999px;
  font-size: 10px;
  white-space: nowrap;
}

.lyric-box {
  height: 108px;
  margin: 15px 0 8px;
  padding: 42px 12px;
  overflow-y: auto;
  text-align: center;
  background: linear-gradient(180deg, rgba(247, 248, 252, 0.92), rgba(251, 251, 253, 0.72));
  border: 1px solid #f0f1f5;
  border-radius: 14px;
  mask-image: linear-gradient(to bottom, transparent, #000 20%, #000 80%, transparent);
  scrollbar-width: none;
}

.lyric-box::-webkit-scrollbar {
  display: none;
}

.lyric-line {
  padding: 6px 0;
  color: #aab1bf;
  font-size: 12px;
  line-height: 1.4;
  transition: color 0.2s ease, transform 0.2s ease;
}

.lyric-line.active {
  color: #3f82df;
  font-size: 13px;
  font-weight: 650;
  transform: scale(1.02);
}

.lyric-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 76px;
  margin: 14px 0 7px;
  color: #b2b8c4;
  background: #fafbfc;
  border-radius: 14px;
  font-size: 12px;
}

.time-row {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  color: #949cad;
  font-size: 10px;
  font-variant-numeric: tabular-nums;
}

.progress-range,
.volume-popover input {
  width: 100%;
  height: 4px;
  margin: 7px 0 2px;
  appearance: none;
  border-radius: 99px;
  outline: none;
  background: linear-gradient(
    90deg,
    #6daef7 0,
    #a9d9ff var(--progress),
    color-mix(in srgb, var(--text-color) 12%, transparent) var(--progress),
    color-mix(in srgb, var(--text-color) 12%, transparent) 100%
  );
  cursor: pointer;
}

.progress-range::-webkit-slider-thumb,
.volume-popover input::-webkit-slider-thumb {
  width: 12px;
  height: 12px;
  appearance: none;
  background: #fff;
  border: 3px solid #5b9cf0;
  border-radius: 50%;
  box-shadow: 0 2px 7px rgba(52, 62, 91, 0.2);
}

.panel-controls {
  position: relative;
  justify-content: center;
  gap: 20px;
  margin: 15px 0 17px;
}

.control-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: transparent;
  cursor: pointer;
}

.control-btn {
  width: 28px;
  height: 28px;
  color: #677287;
  font-size: 13px;
  transition: color 0.2s ease, transform 0.2s ease;
}

.control-btn:hover {
  color: #4d8fea;
  transform: scale(1.1);
}

.volume-wrap {
  position: relative;
}

.volume-popover {
  position: absolute;
  bottom: 34px;
  left: 50%;
  display: flex;
  align-items: center;
  gap: 8px;
  width: 132px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid #eceef4;
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(38, 49, 76, 0.14);
  transform: translateX(-50%);
}

.volume-popover button {
  padding: 0;
  color: #677287;
  background: transparent;
  cursor: pointer;
}

.volume-popover input {
  flex: 1;
  margin: 0;
}

.playlist-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #788296;
  font-size: 11px;
  font-weight: 650;
}

.playlist-head span:last-child {
  color: #abb2c0;
  font-weight: 400;
}

.playlist {
  max-height: 196px;
  margin: 0 -5px;
  padding: 0 5px;
  scrollbar-color: #e2e5ec transparent;
  scrollbar-width: thin;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 7px 8px;
  color: #3e485b;
  text-align: left;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.playlist-item:hover {
  background: #f6f7fa;
}

.playlist-item.active {
  color: #3f82df;
  background: #eef7ff;
}

.playlist-item .num {
  width: 20px;
  color: inherit;
  font-size: 10px;
  font-variant-numeric: tabular-nums;
}

.item-cover {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  overflow: hidden;
  color: #5b9bea;
  background: #eff8ff;
  border-radius: 9px;
}

.item-info {
  min-width: 0;
  gap: 2px;
}

.playlist-item .name,
.playlist-item .singer {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-item .name {
  color: inherit;
  font-size: 12px;
}

.playlist-item .singer {
  color: #9ca4b4;
  font-size: 10px;
}

.item-duration {
  color: #a4acba;
  font-size: 10px;
  font-variant-numeric: tabular-nums;
}

.music-pop-enter-active,
.music-pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.music-pop-enter-from,
.music-pop-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-7px) scale(0.98);
}

@keyframes cover-spin {
  to {
    transform: rotate(360deg);
  }
}

/* 用户端音乐播放器视觉升级 */
.music-mini {
  height: 38px;
  padding: 0 7px;
  border-radius: 16px;
  transition: background-color 0.22s ease;
}

.music-mini:hover,
.music-mini.is-open {
  background: color-mix(in srgb, var(--card-bg-hover) 88%, transparent);
}

.music-cover {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow:
    0 0 0 3px color-mix(in srgb, #5a9cf0 13%, transparent),
    0 7px 18px rgba(79, 145, 237, 0.24);
}

.music-right {
  width: 148px;
  gap: 2px;
}

.music-track-title {
  justify-content: flex-start;
}

.music-title-main {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex: 1;
}

.music-equalizer {
  display: inline-flex;
  align-items: flex-end;
  justify-content: center;
  gap: 1.5px;
  width: 11px;
  height: 11px;
  flex: 0 0 11px;
}

.music-equalizer i,
.playing-bars i {
  display: block;
  width: 2px;
  height: 3px;
  background: linear-gradient(to top, #5c9ef1, #b5e2ff);
  border-radius: 2px;
  transform-origin: bottom;
}

.music-equalizer.playing i {
  animation: music-bar 0.75s ease-in-out infinite alternate;
}

.music-equalizer.playing i:nth-child(2) {
  animation-delay: -0.35s;
}

.music-equalizer.playing i:nth-child(3) {
  animation-delay: -0.18s;
}

.music-btns {
  gap: 11px;
}

.mini-control {
  opacity: 0.5;
}

.progress-bar-mini {
  height: 2px;
  margin-top: 1px;
}

.music-panel {
  isolation: isolate;
  display: flex;
  flex-direction: column;
  width: min(420px, calc(100vw - 32px));
  max-height: min(78vh, 650px);
  padding: 20px;
  color: var(--text-color);
  background:
    linear-gradient(155deg, color-mix(in srgb, #d8edff 34%, transparent), transparent 66%),
    color-mix(in srgb, var(--card-secound-bg, #fff) 92%, transparent);
  border-color: color-mix(in srgb, var(--border-color) 13%, transparent);
  border-radius: 24px;
  box-shadow:
    0 30px 72px rgba(22, 33, 58, 0.24),
    0 4px 16px rgba(22, 33, 58, 0.08);
}

.panel-backdrop {
  position: absolute;
  inset: -34px;
  z-index: -2;
  background-position: center;
  background-size: cover;
  filter: blur(34px) saturate(1.2);
  opacity: 0.14;
  transform: scale(1.15);
  pointer-events: none;
}

.panel-glow {
  position: absolute;
  top: -70px;
  right: -50px;
  z-index: -1;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(111, 178, 250, 0.28), transparent 68%);
  pointer-events: none;
}

.panel-kicker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: color-mix(in srgb, var(--text-color) 42%, transparent);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.kicker-now {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.kicker-now i {
  width: 5px;
  height: 5px;
  background: #68a9f3;
  border-radius: 50%;
  box-shadow: 0 0 0 3px rgba(104, 169, 243, 0.12);
}

.kicker-now i.playing {
  animation: now-pulse 1.5s ease-in-out infinite;
}

.panel-head {
  gap: 14px;
  margin-top: 14px;
}

.cover-big {
  position: relative;
  width: 68px;
  height: 68px;
  flex-basis: 68px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 20px;
  box-shadow: 0 14px 30px rgba(76, 142, 238, 0.25);
}

.cover-big::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.24), transparent 48%);
  border-radius: inherit;
  content: '';
  pointer-events: none;
}

.cover-orbit {
  position: absolute;
  inset: -5px;
  z-index: -1;
  border: 1px solid rgba(104, 169, 243, 0.35);
  border-radius: 24px;
  opacity: 0;
}

.cover-big.playing .cover-orbit {
  opacity: 1;
  animation: cover-orbit 2.4s ease-out infinite;
}

.panel-head .song-name {
  color: var(--text-color);
  font-size: 16px;
  font-weight: 760;
}

.panel-head .singer,
.track-count {
  color: color-mix(in srgb, var(--text-color) 55%, transparent);
}

.mode-label {
  align-self: center;
  padding: 5px 9px;
  color: #4384db;
  background: color-mix(in srgb, #5b9cf0 12%, transparent);
  border: 1px solid color-mix(in srgb, #5b9cf0 17%, transparent);
}

.lyric-box {
  height: 126px;
  margin: 16px 0 8px;
  padding: 50px 14px;
  background:
    linear-gradient(145deg, rgba(220, 241, 255, 0.2), transparent 62%),
    color-mix(in srgb, var(--card-bg) 66%, transparent);
  border-color: color-mix(in srgb, var(--border-color) 8%, transparent);
  border-radius: 17px;
  scroll-behavior: smooth;
}

.lyric-line {
  color: color-mix(in srgb, var(--text-color) 38%, transparent);
  font-size: 12px;
}

.lyric-line.active {
  color: #3b7fe0;
  font-size: 14px;
  font-weight: 700;
  text-shadow: 0 3px 12px rgba(72, 139, 226, 0.18);
}

.lyric-empty {
  gap: 8px;
  height: 82px;
  color: color-mix(in srgb, var(--text-color) 38%, transparent);
  background: color-mix(in srgb, var(--card-bg) 62%, transparent);
  border: 1px dashed color-mix(in srgb, var(--border-color) 10%, transparent);
}

.time-row {
  margin-top: 10px;
  color: color-mix(in srgb, var(--text-color) 43%, transparent);
}

.progress-range {
  margin-top: 5px;
}

.panel-controls {
  width: fit-content;
  margin: 13px auto 18px;
  padding: 7px 18px;
  gap: 18px;
  background: color-mix(in srgb, var(--card-bg) 72%, transparent);
  border: 1px solid color-mix(in srgb, var(--border-color) 8%, transparent);
  border-radius: 999px;
}

.control-btn {
  color: color-mix(in srgb, var(--text-color) 64%, transparent);
}

.volume-popover {
  background: color-mix(in srgb, var(--card-secound-bg, #fff) 96%, transparent);
  border-color: color-mix(in srgb, var(--border-color) 9%, transparent);
  backdrop-filter: blur(14px);
}

.volume-popover button {
  color: color-mix(in srgb, var(--text-color) 64%, transparent);
}

.playlist-head {
  align-items: center;
  margin-bottom: 9px;
  color: color-mix(in srgb, var(--text-color) 54%, transparent);
  letter-spacing: 0.04em;
}

.playlist-head > span:first-child {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.playlist-head > span:last-child {
  color: color-mix(in srgb, var(--text-color) 32%, transparent);
  font-size: 9px;
  letter-spacing: 0.1em;
}

.playlist {
  max-height: 194px;
  padding-right: 7px;
}

.playlist-item {
  position: relative;
  overflow: hidden;
  color: color-mix(in srgb, var(--text-color) 78%, transparent);
  border: 1px solid transparent;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    transform 0.2s ease;
}

.playlist-item:hover {
  background: color-mix(in srgb, var(--card-bg-hover) 82%, transparent);
  transform: translateX(2px);
}

.playlist-item.active {
  color: #3f82df;
  background: linear-gradient(90deg, rgba(91, 156, 240, 0.13), rgba(153, 211, 255, 0.07));
  border-color: rgba(91, 156, 240, 0.12);
}

.playlist-item.active::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 2px;
  height: 20px;
  background: linear-gradient(#8eceff, #4e90ea);
  border-radius: 4px;
  transform: translateY(-50%);
  content: '';
}

.playlist-item .singer,
.item-duration {
  color: color-mix(in srgb, var(--text-color) 38%, transparent);
}

.item-cover {
  width: 36px;
  height: 36px;
  flex-basis: 36px;
  color: #5b9bea;
  background: color-mix(in srgb, #5b9bea 11%, transparent);
  border: 1px solid rgba(91, 155, 234, 0.1);
}

.playing-bars {
  display: inline-flex;
  align-items: flex-end;
  justify-content: center;
  gap: 1.5px;
  width: 100%;
  height: 12px;
}

.playing-bars i {
  animation: music-bar 0.72s ease-in-out infinite alternate;
}

.playing-bars i:nth-child(2) {
  animation-delay: -0.42s;
}

.playing-bars i:nth-child(3) {
  animation-delay: -0.2s;
}

@keyframes music-bar {
  0% {
    height: 3px;
  }

  100% {
    height: 11px;
  }
}

@keyframes now-pulse {
  50% {
    box-shadow: 0 0 0 6px rgba(104, 169, 243, 0.06);
    opacity: 0.72;
  }
}

@keyframes cover-orbit {
  from {
    opacity: 0.55;
    transform: scale(0.94);
  }

  to {
    opacity: 0;
    transform: scale(1.16);
  }
}

@media (max-width: 1200px) {
  .music-right {
    width: 122px;
  }

  .music-singer {
    display: none;
  }
}

@media (max-width: 520px) {
  .music-panel {
    position: fixed;
    top: 82px;
    left: 50%;
    width: min(94vw, 420px);
    max-height: calc(100vh - 96px);
    padding: 16px;
    border-radius: 22px;
  }

  .lyric-box {
    height: 110px;
  }

  .panel-controls {
    gap: 14px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .music-cover.playing img,
  .music-equalizer.playing i,
  .playing-bars i,
  .kicker-now i.playing,
  .cover-big.playing .cover-orbit {
    animation: none;
  }
}
</style>

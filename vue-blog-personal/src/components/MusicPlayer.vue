<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const audio = ref(null)
const showPanel = ref(false)
const isPlaying = ref(false)
const progress = ref(0)
const volume = ref(70)
const showVolume = ref(false)

const loopMode = ref(0)
const loopIcon = computed(() => {
  switch (loopMode.value) {
    case 0: return ['fas', 'repeat']           // 列表循环
    case 1: return ['fas', 'arrow-rotate-right']         // 单曲循环
    case 2: return ['fas', 'shuffle']          // 随机播放
    default: return ['fas', 'repeat']
  }
})

const loopText = computed(() => {
  switch (loopMode.value) {
    case 0: return '列表循环'
    case 1: return '单曲循环'
    case 2: return '随机播放'
    default: return '列表循环'
  }
})

const playlist = ref([{
  name: '默认音乐',
  singer: '点击上传添加',
  url: '',
  cover: 'https://picsum.photos/40/40',
  lyric: ['等待播放...']
}])

const currentIndex = ref(0)
const currentSong = computed(() => playlist.value[currentIndex.value])
const currentLyric = ref('等待播放...')
const musicContainer = ref(null)
const uploadInput = ref(null)

const toggleShowPanel = () => {
  showPanel.value = !showPanel.value
}

const initAudio = () => {
  if (!audio.value) {
    audio.value = new Audio()
    audio.value.volume = volume.value / 100
    audio.value.addEventListener('timeupdate', updateProgress)
    audio.value.addEventListener('ended', handleSongEnd)
  }
}

const updateProgress = () => {
  if (!audio.value) return
  const cur = audio.value.currentTime
  const total = audio.value.duration
  progress.value = (cur / total) * 100 || 0

  const lyric = currentSong.value.lyric || []
  if (lyric.length) {
    const line = Math.floor((progress.value / 100) * lyric.length)
    currentLyric.value = lyric[Math.min(line, lyric.length - 1)]
  }
}

const togglePlay = () => {
  if (!audio.value) initAudio()
  if (!currentSong.value.url) {
    ElMessage.warning('请先添加音乐！')
    return
  }
  isPlaying.value ? audio.value.pause() : audio.value.play()
  isPlaying.value = !isPlaying.value
}

const prevSong = () => {
  if (currentIndex.value <= 0) return
  currentIndex.value--
  switchSong(currentIndex.value)
}

const nextSong = () => {
  if (currentIndex.value >= playlist.value.length - 1) return
  currentIndex.value++
  switchSong(currentIndex.value)
}

const switchSong = (index) => {
  currentIndex.value = index
  audio.value.src = playlist.value[index].url
  audio.value.currentTime = 0
  progress.value = 0
  audio.value.play()
  isPlaying.value = true
}

const deleteSong = (index) => {
  if (playlist.value.length <= 1) return
  playlist.value.splice(index, 1)
  if (currentIndex.value === index) {
    currentIndex.value = Math.min(currentIndex.value, playlist.value.length - 1)
    if (currentSong.value.url) switchSong(currentIndex.value)
  } else if (currentIndex.value > index) {
    currentIndex.value--
  }
}

const changeProgress = (e) => {
  if (!audio.value || !audio.value.duration) return
  const rect = e.target.getBoundingClientRect()
  const per = (e.clientX - rect.left) / rect.width
  audio.value.currentTime = per * audio.value.duration
  progress.value = per * 100
}

const handleSongEnd = () => {
  if (loopMode.value === 1) {
    audio.value.currentTime = 0
    audio.value.play()
  } else if (loopMode.value === 0) {
    nextSong()
  } else {
    isPlaying.value = false
  }
}

const changeLoopMode = () => {
  loopMode.value = (loopMode.value + 1) % 3
}

const setVolume = () => {
  if (audio.value) audio.value.volume = volume.value / 100
}

const openUpload = () => {
  uploadInput.value.click()
}

const handleUpload = (e) => {
  const file = e.target.files[0]
  if (!file) return
  const url = URL.createObjectURL(file)
  const name = file.name.replace(/\.[^/.]+$/, '')
  playlist.value.push({
    name: name || '未知歌曲',
    singer: '本地音乐',
    url: url,
    cover: 'https://picsum.photos/40/40',
    lyric: [name + ' - 本地音乐']
  })
  ElMessage.success('添加成功！')
  uploadInput.value.value = ''
}

onMounted(() => {
  initAudio()
  window.addEventListener('click', clickOutside)
})

onUnmounted(() => {
  window.removeEventListener('click', clickOutside)
  if (audio.value) {
    audio.value.pause()
    audio.value = null
  }
})

// 🔥 修复：不关闭音乐面板
const clickOutside = (e) => {
  if (musicContainer.value && !musicContainer.value.contains(e.target)) {
    showPanel.value = false
    showVolume.value = false
  }
}
</script>

<template>
  <div class="music-container" ref="musicContainer">
    <!-- 导航栏小模式：点击展开 -->
    <div class="music-mini" @click="toggleShowPanel">
      <div class="music-cover">
        <img :src="currentSong.cover" alt="cover" />
      </div>

      <div class="music-right">
        <div class="music-btns">
          <span class="music-label">音乐</span>
          <el-button link class="control-btn" @click.stop="prevSong">
            <font-awesome-icon :icon="['fas', 'backward-step']" />
          </el-button>
          <el-button link @click.stop="togglePlay">
            <font-awesome-icon :icon="isPlaying ? 'fa-pause' : 'fa-play'" />
          </el-button>
          <el-button link class="control-btn" @click.stop="nextSong">
            <font-awesome-icon :icon="['fas', 'forward-step']" />
          </el-button>
        </div>
        <div class="progress-bar-mini" @click.stop="changeProgress">
          <div class="progress" :style="{ width: progress + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- 下拉面板 -->
    <div class="music-panel" v-show="showPanel" @click.stop>
      <div class="panel-top">
        <span class="music-label">音乐</span>
        <div class="song-info">
          <span class="song-name">{{ currentSong.name }}</span>
          <span class="singer">{{ currentSong.singer }}</span>
        </div>
        <div class="cover-big">
          <img :src="currentSong.cover" alt="cover" />
        </div>
        <div class="lyric">{{ currentLyric }}</div>
      </div>

      <div class="panel-controls">
        <el-button link class="control-btn" @click.stop="changeLoopMode" :title="loopText">
          <font-awesome-icon :icon="loopIcon" />
        </el-button>

        <el-button link class="control-btn" @click.stop="prevSong" title="上一曲">
          <font-awesome-icon :icon="['fas', 'backward-step']" />
        </el-button>
        <el-button link @click.stop="togglePlay" :title="isPlaying ? '暂停' : '播放'">
          <font-awesome-icon :icon="isPlaying ? 'fa-pause' : 'fa-play'" />
        </el-button>
        <el-button link class="control-btn" @click.stop="nextSong" title="下一曲">
          <font-awesome-icon :icon="['fas', 'forward-step']" />
        </el-button>

        <el-button link class="control-btn" @click.stop="showVolume = !showVolume" title="音量">
          <font-awesome-icon :icon="['fas', 'volume-up']" />
          <el-slider v-if="showVolume" v-model="volume" @change="setVolume" style="left: 5px;width:100px" />
        </el-button>
      </div>

      <div class="progress-bar" @click.stop="changeProgress">
        <div class="progress" :style="{ width: progress + '%' }"></div>
      </div>

      <div class="divider"></div>

      <div class="playlist">
        <div class="playlist-item" v-for="(item, index) in playlist" :key="index"
          :class="{ active: currentIndex === index }" @click.stop="switchSong(index)">
          <span class="num">{{ index + 1 }}</span>
          <div class="item-cover">
            <img :src="item.cover" alt="cover" />
          </div>
          <div class="item-info">
            <div class="name">{{ item.name }}</div>
            <div class="singer">{{ item.singer }}</div>
          </div>
          <el-button link icon="Close" @click.stop="deleteSong(index)"></el-button>
        </div>

        <div class="add-music">
          <el-button type="primary" link size="small" @click.stop="openUpload">+ 添加音乐</el-button>
          <input ref="uploadInput" type="file" accept="audio/*" style="display:none" @change="handleUpload" />
        </div>
      </div>
    </div>
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
</style>
<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Delete,
  Edit,
  Headset,
  Close,
  Mute,
  Plus,
  Refresh,
  Search,
  UploadFilled,
  VideoPause,
  VideoPlay,
} from '@element-plus/icons-vue';
import MyPagination from '@/components/MyPagination.vue';
import PermissionViewTip from '@/components/PermissionViewTip.vue';
import { uploadApi } from '@/api/upload';
import {
  addMusicApi,
  deleteMusicApi,
  getMusicDetailApi,
  getMusicListApi,
  updateMusicApi,
  updateMusicStatusApi,
} from '@/api/music';

const AUDIO_MAX_SIZE = 50 * 1024 * 1024;
const COVER_MAX_SIZE = 5 * 1024 * 1024;
const LYRIC_MAX_SIZE = 2 * 1024 * 1024;
const AUDIO_EXTENSIONS = ['mp3', 'm4a', 'aac', 'wav', 'flac', 'ogg'];

const loading = ref(false);
const musicList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const selectedIds = ref([]);
const statusLoadingId = ref(null);
const previewAudioEl = ref(null);
const previewRow = ref(null);
const previewPlaying = ref(false);
const previewCurrentTime = ref(0);
const previewDuration = ref(0);
const previewVolume = ref(80);
let lastPreviewVolume = 80;

const queryForm = reactive({
  keyword: '',
  status: '',
});

const getMusicList = async () => {
  loading.value = true;
  try {
    const result = await getMusicListApi({
      keyword: queryForm.keyword.trim() || undefined,
      status: queryForm.status === '' ? undefined : queryForm.status,
      page: currentPage.value,
      pageSize: pageSize.value,
    });
    if (result.code !== 200) {
      ElMessage.error(result.msg || '获取音乐列表失败');
      musicList.value = [];
      total.value = 0;
      return;
    }
    musicList.value = result.data?.rows || [];
    total.value = Number(result.data?.total) || 0;
  } catch (error) {
    console.error('获取音乐列表异常', error);
    musicList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  getMusicList();
};

const resetQuery = () => {
  queryForm.keyword = '';
  queryForm.status = '';
  currentPage.value = 1;
  getMusicList();
};

const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map((row) => row.id);
};

const dialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref(null);
const audioFileList = ref([]);
const coverFileList = ref([]);
const lyricFileList = ref([]);

const musicForm = reactive({
  id: null,
  name: '',
  singer: '',
  url: '',
  cover: '',
  lyric: '',
  duration: 0,
  sort: 0,
  status: 1,
});

const rules = {
  name: [
    { required: true, message: '请输入歌曲名称', trigger: 'blur' },
    { max: 100, message: '歌曲名称不能超过100个字符', trigger: 'blur' },
  ],
  url: [{ required: true, message: '请上传音频文件', trigger: 'change' }],
};

const dialogTitle = computed(() => (isEdit.value ? '编辑音乐' : '新增音乐'));

const resetForm = () => {
  Object.assign(musicForm, {
    id: null,
    name: '',
    singer: '',
    url: '',
    cover: '',
    lyric: '',
    duration: 0,
    sort: 0,
    status: 1,
  });
  audioFileList.value = [];
  coverFileList.value = [];
  lyricFileList.value = [];
  formRef.value?.clearValidate();
};

const openAddDialog = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const openEditDialog = async (row) => {
  isEdit.value = true;
  resetForm();
  try {
    const result = await getMusicDetailApi(row.id);
    if (result.code !== 200 || !result.data) {
      ElMessage.error(result.msg || '获取音乐详情失败');
      return;
    }
    const detail = result.data;
    Object.assign(musicForm, {
      id: detail.id,
      name: detail.name || '',
      singer: detail.singer || '',
      url: detail.url || '',
      cover: detail.cover || '',
      lyric: detail.lyric || '',
      duration: Number(detail.duration) || 0,
      sort: Number(detail.sort) || 0,
      status: detail.status === 0 ? 0 : 1,
    });
    audioFileList.value = detail.url ? [{ name: getFileName(detail.url), url: detail.url }] : [];
    coverFileList.value = detail.cover ? [{ name: getFileName(detail.cover), url: detail.cover }] : [];
    lyricFileList.value = detail.lyric ? [{ name: getFileName(detail.lyric), url: detail.lyric }] : [];
    dialogVisible.value = true;
  } catch (error) {
    console.error('获取音乐详情异常', error);
  }
};

const getFileName = (url) => {
  try {
    const path = decodeURIComponent(String(url).split('?')[0]);
    return path.split('/').filter(Boolean).pop() || '文件';
  } catch (error) {
    return String(url).split('/').filter(Boolean).pop() || '文件';
  }
};

const getExtension = (name) => {
  const parts = String(name || '').toLowerCase().split('.');
  return parts.length > 1 ? parts.pop() : '';
};

const beforeAudioUpload = (file) => {
  const extension = getExtension(file.name);
  if (!file.type?.startsWith('audio/') && !AUDIO_EXTENSIONS.includes(extension)) {
    ElMessage.warning('请上传 mp3、m4a、aac、wav、flac 或 ogg 音频');
    return false;
  }
  if (file.size > AUDIO_MAX_SIZE) {
    ElMessage.warning('音频文件不能超过 50MB');
    return false;
  }
  return true;
};

const beforeCoverUpload = (file) => {
  if (!file.type?.startsWith('image/')) {
    ElMessage.warning('封面只能上传图片');
    return false;
  }
  if (file.size > COVER_MAX_SIZE) {
    ElMessage.warning('封面图片不能超过 5MB');
    return false;
  }
  return true;
};

const beforeLyricUpload = (file) => {
  if (getExtension(file.name) !== 'lrc' && file.type && !file.type.startsWith('text/')) {
    ElMessage.warning('歌词请上传 .lrc 文件');
    return false;
  }
  if (file.size > LYRIC_MAX_SIZE) {
    ElMessage.warning('歌词文件不能超过 2MB');
    return false;
  }
  return true;
};

const customUpload = (options, type) => {
  const formData = new FormData();
  formData.append('file', options.file);
  uploadApi(formData)
    .then((result) => {
      if (result.code !== 200 || !result.data) {
        const error = new Error(result.msg || '上传失败');
        ElMessage.error(error.message);
        options.onError(error);
        return;
      }
      options.onSuccess(result.data);
    })
    .catch((error) => {
      console.error(`${type}上传异常`, error);
      options.onError(error);
    });
};

const uploadAudio = (options) => customUpload(options, '音频');
const uploadCover = (options) => customUpload(options, '封面');
const uploadLyric = (options) => customUpload(options, '歌词');

const handleAudioSuccess = (url, uploadFile) => {
  musicForm.url = url;
  uploadFile.url = url;
  readAudioDuration(url);
  formRef.value?.validateField('url');
};

const handleCoverSuccess = (url, uploadFile) => {
  musicForm.cover = url;
  uploadFile.url = url;
};

const handleLyricSuccess = (url, uploadFile) => {
  musicForm.lyric = url;
  uploadFile.url = url;
};

const handleAudioRemove = () => {
  musicForm.url = '';
  musicForm.duration = 0;
};

const handleCoverRemove = () => {
  musicForm.cover = '';
};

const handleLyricRemove = () => {
  musicForm.lyric = '';
};

const readAudioDuration = (url) => {
  const audio = new Audio();
  audio.preload = 'metadata';
  audio.onloadedmetadata = () => {
    if (Number.isFinite(audio.duration)) {
      musicForm.duration = Math.round(audio.duration);
    }
    audio.removeAttribute('src');
  };
  audio.onerror = () => {
    audio.removeAttribute('src');
  };
  audio.src = url;
};

const closeDialog = () => {
  if (submitLoading.value) return;
  dialogVisible.value = false;
};

const submitForm = async () => {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitLoading.value = true;
  try {
    const payload = {
      id: musicForm.id,
      name: musicForm.name.trim(),
      singer: musicForm.singer.trim(),
      url: musicForm.url,
      cover: musicForm.cover,
      lyric: musicForm.lyric,
      duration: Number(musicForm.duration) || 0,
      sort: Number(musicForm.sort) || 0,
      status: musicForm.status,
    };
    const result = isEdit.value ? await updateMusicApi(payload) : await addMusicApi(payload);
    if (result.code !== 200) {
      ElMessage.error(result.msg || '保存失败');
      return;
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功');
    dialogVisible.value = false;
    getMusicList();
  } catch (error) {
    console.error('保存音乐异常', error);
  } finally {
    submitLoading.value = false;
  }
};

const changeStatus = async (row, enabled) => {
  const status = enabled ? 1 : 0;
  statusLoadingId.value = row.id;
  try {
    const result = await updateMusicStatusApi(row.id, status);
    if (result.code !== 200) {
      ElMessage.error(result.msg || '状态修改失败');
      return;
    }
    row.status = status;
    ElMessage.success(enabled ? '已上架' : '已下架');
  } catch (error) {
    console.error('修改音乐状态异常', error);
  } finally {
    statusLoadingId.value = null;
  }
};

const deleteMusic = async (ids) => {
  if (!ids?.length) {
    ElMessage.warning('请选择要删除的音乐');
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${ids.length} 首音乐吗？删除后将不再展示。`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    if (previewRow.value && ids.includes(previewRow.value.id)) {
      closePreview();
    }
    const result = await deleteMusicApi(ids);
    if (result.code !== 200) {
      ElMessage.error(result.msg || '删除失败');
      return;
    }
    ElMessage.success('删除成功');
    const maxPage = Math.max(1, Math.ceil((total.value - ids.length) / pageSize.value));
    currentPage.value = Math.min(currentPage.value, maxPage);
    getMusicList();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除音乐异常', error);
    }
  }
};

const handleBatchDelete = () => deleteMusic(selectedIds.value);

const previewProgress = computed(() => {
  if (!previewDuration.value) return 0;
  return Math.min(100, (previewCurrentTime.value / previewDuration.value) * 100);
});

const playPreview = async () => {
  if (!previewAudioEl.value) return;
  try {
    await previewAudioEl.value.play();
  } catch (error) {
    console.error('试听播放失败', error);
    ElMessage.error('试听失败，请检查音频地址是否可访问');
  }
};

const togglePreview = async (row) => {
  if (!row?.url) {
    ElMessage.warning('该歌曲没有可试听的音频地址');
    return;
  }
  if (!previewRow.value || previewRow.value.id !== row.id) {
    previewRow.value = row;
    previewPlaying.value = false;
    previewCurrentTime.value = 0;
    previewDuration.value = Number(row.duration) || 0;
    await nextTick();
    if (previewAudioEl.value) {
      previewAudioEl.value.volume = previewVolume.value / 100;
      previewAudioEl.value.load();
    }
    await playPreview();
    return;
  }
  if (previewAudioEl.value?.paused) {
    await playPreview();
  } else {
    previewAudioEl.value?.pause();
  }
};

const handlePreviewLoaded = () => {
  previewDuration.value = Number(previewAudioEl.value?.duration) || Number(previewRow.value?.duration) || 0;
};

const handlePreviewTimeUpdate = () => {
  previewCurrentTime.value = Number(previewAudioEl.value?.currentTime) || 0;
};

const handlePreviewEnded = () => {
  previewPlaying.value = false;
  previewCurrentTime.value = 0;
};

const handlePreviewError = () => {
  previewPlaying.value = false;
  ElMessage.error('音频加载失败，请检查文件地址');
};

const seekPreview = (event) => {
  if (!previewAudioEl.value || !previewDuration.value) return;
  const percent = Number(event.target.value) || 0;
  previewAudioEl.value.currentTime = (percent / 100) * previewDuration.value;
  previewCurrentTime.value = previewAudioEl.value.currentTime;
};

const changePreviewVolume = (event) => {
  previewVolume.value = Number(event.target.value) || 0;
  if (previewVolume.value > 0) lastPreviewVolume = previewVolume.value;
  if (previewAudioEl.value) {
    previewAudioEl.value.volume = previewVolume.value / 100;
  }
};

const togglePreviewMute = () => {
  previewVolume.value = previewVolume.value > 0 ? 0 : lastPreviewVolume || 80;
  if (previewAudioEl.value) {
    previewAudioEl.value.volume = previewVolume.value / 100;
  }
};

const closePreview = () => {
  if (previewAudioEl.value) {
    previewAudioEl.value.pause();
  }
  previewRow.value = null;
  previewPlaying.value = false;
  previewCurrentTime.value = 0;
  previewDuration.value = 0;
};

const formatPlaybackTime = (seconds) => {
  const value = Math.max(0, Math.floor(Number(seconds) || 0));
  const minutes = Math.floor(value / 60);
  const rest = value % 60;
  return `${String(minutes).padStart(2, '0')}:${String(rest).padStart(2, '0')}`;
};

const formatDuration = (seconds) => {
  const value = Number(seconds) || 0;
  if (!value) return '--:--';
  const minutes = Math.floor(value / 60);
  const rest = Math.floor(value % 60);
  return `${String(minutes).padStart(2, '0')}:${String(rest).padStart(2, '0')}`;
};

onMounted(getMusicList);
onUnmounted(closePreview);
</script>

<template>
  <div class="music-management">
    <div class="page-header">
      <div>
        <h1>音乐管理</h1>
        <p>维护用户端导航栏播放器中的歌曲、封面与歌词</p>
      </div>
      <div class="header-buttons">
        <el-button v-perm="'sys:music:add'" type="primary" :icon="Plus" @click="openAddDialog">
          新增音乐
        </el-button>
        <el-button v-perm="'sys:music:delete'" type="danger" plain :icon="Delete" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </div>

    <PermissionViewTip :perms="['sys:music:add', 'sys:music:edit', 'sys:music:delete']" />

    <el-card class="query-card" shadow="never">
      <el-form :model="queryForm" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="歌曲查询">
          <el-input
            v-model="queryForm.keyword"
            clearable
            placeholder="搜索歌名或歌手"
            style="width: 260px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态" style="width: 150px">
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="musicList"
        row-key="id"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="封面" width="90" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.cover"
              :src="row.cover"
              fit="cover"
              class="music-cover"
              :preview-src-list="[row.cover]"
              preview-teleported
            />
            <div v-else class="music-cover cover-placeholder">
              <el-icon><Headset /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="歌曲" min-width="210">
          <template #default="{ row }">
            <div class="song-name">{{ row.name }}</div>
            <div class="song-singer">{{ row.singer || '未知歌手' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="100" align="center">
          <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-perm="'sys:music:edit'"
              :model-value="row.status === 1"
              :loading="statusLoadingId === row.id"
              inline-prompt
              active-text="上架"
              inactive-text="下架"
              @change="(value) => changeStatus(row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180" align="center">
          <template #default="{ row }">{{ row.updateTime || row.createTime || '——' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="previewRow?.id === row.id && previewPlaying ? 'warning' : 'primary'"
              :icon="previewRow?.id === row.id && previewPlaying ? VideoPause : VideoPlay"
              @click="togglePreview(row)"
            >
              {{ previewRow?.id === row.id && previewPlaying ? '暂停' : '试听' }}
            </el-button>
            <el-button v-perm="'sys:music:edit'" link type="primary" :icon="Edit" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button v-perm="'sys:music:delete'" link type="danger" :icon="Delete" @click="deleteMusic([row.id])">
              删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无音乐，请新增第一首歌曲" />
        </template>
      </el-table>

      <div class="pagination-box">
        <MyPagination
          :total="total"
          :current-page="currentPage"
          :page-size="pageSize"
          @update:current-page="currentPage = $event"
          @update:page-size="pageSize = $event"
          @change="getMusicList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="closeDialog"
      destroy-on-close
      class="music-dialog"
    >
      <el-form ref="formRef" :model="musicForm" :rules="rules" label-width="88px">
        <div class="form-grid">
          <el-form-item label="歌曲名称" prop="name">
            <el-input v-model="musicForm.name" maxlength="100" placeholder="请输入歌曲名称" />
          </el-form-item>
          <el-form-item label="歌手">
            <el-input v-model="musicForm.singer" maxlength="100" placeholder="未知歌手" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="musicForm.sort" :min="0" :max="99999" controls-position="right" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="musicForm.status">
              <el-radio :value="1">上架</el-radio>
              <el-radio :value="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item label="音频文件" prop="url" class="upload-item">
          <el-upload
            v-model:file-list="audioFileList"
            :http-request="uploadAudio"
            :before-upload="beforeAudioUpload"
            :on-success="handleAudioSuccess"
            :on-remove="handleAudioRemove"
            :limit="1"
            accept=".mp3,.m4a,.aac,.wav,.flac,.ogg,audio/*"
          >
            <el-button :icon="UploadFilled">选择音频</el-button>
            <template #tip>
              <div class="upload-tip">支持 mp3、m4a、aac、wav、flac、ogg，最大 50MB</div>
            </template>
          </el-upload>
          <div v-if="musicForm.duration" class="duration-tip">
            自动识别时长：{{ formatDuration(musicForm.duration) }}
          </div>
        </el-form-item>

        <el-form-item label="歌曲封面">
          <el-upload
            v-model:file-list="coverFileList"
            :http-request="uploadCover"
            :before-upload="beforeCoverUpload"
            :on-success="handleCoverSuccess"
            :on-remove="handleCoverRemove"
            :limit="1"
            list-type="picture-card"
            accept="image/*"
          >
            <el-icon><UploadFilled /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="LRC 歌词">
          <el-upload
            v-model:file-list="lyricFileList"
            :http-request="uploadLyric"
            :before-upload="beforeLyricUpload"
            :on-success="handleLyricSuccess"
            :on-remove="handleLyricRemove"
            :limit="1"
            accept=".lrc,text/plain"
          >
            <el-button :icon="UploadFilled">选择歌词</el-button>
            <template #tip>
              <div class="upload-tip">可选，上传 .lrc 时间轴歌词文件</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitLoading" @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <transition name="preview-player">
      <div v-if="previewRow" class="audio-preview-player">
        <audio
          ref="previewAudioEl"
          :src="previewRow.url"
          preload="metadata"
          @loadedmetadata="handlePreviewLoaded"
          @timeupdate="handlePreviewTimeUpdate"
          @play="previewPlaying = true"
          @pause="previewPlaying = false"
          @ended="handlePreviewEnded"
          @error="handlePreviewError"
        ></audio>

        <div class="preview-cover" :class="{ playing: previewPlaying }">
          <img v-if="previewRow.cover" :src="previewRow.cover" alt="" />
          <el-icon v-else><Headset /></el-icon>
        </div>
        <div class="preview-info">
          <div class="preview-title">{{ previewRow.name }}</div>
          <div class="preview-singer">{{ previewRow.singer || '未知歌手' }}</div>
        </div>
        <button class="preview-main-btn" type="button" @click="togglePreview(previewRow)">
          <el-icon>
            <VideoPause v-if="previewPlaying" />
            <VideoPlay v-else />
          </el-icon>
        </button>
        <div class="preview-progress">
          <input
            type="range"
            min="0"
            max="100"
            step="0.1"
            :value="previewProgress"
            :style="{ '--progress': `${previewProgress}%` }"
            aria-label="试听进度"
            @input="seekPreview"
          />
          <div class="preview-time">
            <span>{{ formatPlaybackTime(previewCurrentTime) }}</span>
            <span>{{ formatPlaybackTime(previewDuration) }}</span>
          </div>
        </div>
        <div class="preview-volume">
          <button type="button" title="静音" @click="togglePreviewMute">
            <el-icon>
              <Mute v-if="previewVolume === 0" />
              <Headset v-else />
            </el-icon>
          </button>
          <input
            type="range"
            min="0"
            max="100"
            :value="previewVolume"
            :style="{ '--progress': `${previewVolume}%` }"
            aria-label="试听音量"
            @input="changePreviewVolume"
          />
        </div>
        <button class="preview-close" type="button" title="关闭试听" @click="closePreview">
          <el-icon><Close /></el-icon>
        </button>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.music-management {
  min-height: 100%;
  padding: 22px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.page-header h1 {
  margin: 0;
  color: #172033;
  font-size: 26px;
  letter-spacing: 0.02em;
}

.page-header p {
  margin: 7px 0 0;
  color: #7a8499;
  font-size: 13px;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.query-card {
  margin-bottom: 16px;
  border: 0;
  border-radius: 14px;
  box-shadow: 0 8px 30px rgba(35, 47, 75, 0.07);
}

.query-card :deep(.el-card__body) {
  padding-bottom: 2px;
}

.table-card {
  border: 0;
  border-radius: 14px;
  box-shadow: 0 8px 30px rgba(35, 47, 75, 0.07);
}

.table-card :deep(.el-card__body) {
  padding: 14px;
}

.music-cover {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(31, 55, 100, 0.16);
}

.cover-placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef3ff, #dbe8ff);
  color: #5a7fd6;
  font-size: 24px;
}

.song-name {
  color: #1e293b;
  font-weight: 650;
}

.song-singer {
  margin-top: 5px;
  color: #8a94a6;
  font-size: 12px;
}

.pagination-box {
  padding-top: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.form-grid :deep(.el-input-number) {
  width: 100%;
}

.upload-item {
  margin-top: 2px;
}

.upload-tip {
  color: #9099a9;
  font-size: 12px;
  line-height: 1.6;
}

.duration-tip {
  width: 100%;
  margin-top: 4px;
  color: #4b78d0;
  font-size: 12px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item),
:deep(.el-upload--picture-card) {
  width: 86px;
  height: 86px;
}

.audio-preview-player {
  position: fixed;
  right: 28px;
  bottom: 24px;
  z-index: 3000;
  display: grid;
  grid-template-columns: 48px minmax(110px, 1fr) 42px minmax(150px, 240px) 100px 28px;
  align-items: center;
  gap: 12px;
  width: min(760px, calc(100vw - 56px));
  padding: 12px 14px;
  color: #263248;
  background: rgba(255, 255, 255, 0.97);
  border: 1px solid rgba(224, 229, 240, 0.9);
  border-radius: 18px;
  box-shadow: 0 18px 52px rgba(31, 44, 70, 0.2);
  backdrop-filter: blur(18px);
}

.audio-preview-player audio {
  display: none;
}

.preview-cover {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(145deg, #ff866f, #d84e83);
  border-radius: 14px;
  box-shadow: 0 8px 18px rgba(210, 76, 125, 0.22);
  font-size: 20px;
}

.preview-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-cover.playing img {
  animation: preview-cover-spin 12s linear infinite;
}

.preview-info {
  min-width: 0;
}

.preview-title,
.preview-singer {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-title {
  color: #1d293d;
  font-size: 13px;
  font-weight: 700;
}

.preview-singer {
  margin-top: 3px;
  color: #8a94a6;
  font-size: 11px;
}

.preview-main-btn,
.preview-close,
.preview-volume button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  cursor: pointer;
}

.preview-main-btn {
  width: 38px;
  height: 38px;
  color: #fff;
  background: linear-gradient(145deg, #ff7c6c, #d94c82);
  border-radius: 50%;
  box-shadow: 0 7px 17px rgba(211, 76, 125, 0.28);
}

.preview-progress input,
.preview-volume input {
  width: 100%;
  height: 4px;
  margin: 0;
  appearance: none;
  background: linear-gradient(
    90deg,
    #e35b7d 0,
    #f38b72 var(--progress),
    #e8ebf1 var(--progress),
    #e8ebf1 100%
  );
  border-radius: 99px;
  outline: none;
  cursor: pointer;
}

.preview-progress input::-webkit-slider-thumb,
.preview-volume input::-webkit-slider-thumb {
  width: 11px;
  height: 11px;
  appearance: none;
  background: #fff;
  border: 3px solid #df5c7e;
  border-radius: 50%;
  box-shadow: 0 2px 7px rgba(42, 55, 83, 0.18);
}

.preview-time {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  color: #9aa3b3;
  font-size: 9px;
  font-variant-numeric: tabular-nums;
}

.preview-volume {
  display: flex;
  align-items: center;
  gap: 6px;
}

.preview-volume button {
  color: #798397;
  background: transparent;
}

.preview-volume input {
  width: 68px;
}

.preview-close {
  width: 26px;
  height: 26px;
  color: #8c95a6;
  background: #f3f5f8;
  border-radius: 50%;
  transition: color 0.2s ease, background 0.2s ease;
}

.preview-close:hover {
  color: #d94c6f;
  background: #fff0f3;
}

.preview-player-enter-active,
.preview-player-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.preview-player-enter-from,
.preview-player-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

@keyframes preview-cover-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 760px) {
  .music-management {
    padding: 12px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-buttons {
    width: 100%;
  }

  .header-buttons :deep(.el-button) {
    flex: 1;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .audio-preview-player {
    right: 12px;
    bottom: 12px;
    grid-template-columns: 44px minmax(0, 1fr) 38px 28px;
    gap: 10px;
    width: calc(100vw - 24px);
    padding: 11px 12px;
    box-sizing: border-box;
  }

  .preview-progress {
    grid-column: 1 / -1;
  }

  .preview-volume {
    display: none;
  }
}
</style>

<style>
.music-dialog .el-dialog {
  border-radius: 18px;
  overflow: hidden;
}

.music-dialog .el-dialog__header {
  margin-right: 0;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #eef1f6;
}

.music-dialog .el-dialog__body {
  padding: 22px 24px 6px;
}

.music-dialog .el-dialog__footer {
  padding: 14px 24px 20px;
  border-top: 1px solid #eef1f6;
}

@media (max-width: 760px) {
  .music-dialog {
    width: calc(100% - 28px) !important;
    margin-top: 5vh !important;
  }
}
</style>

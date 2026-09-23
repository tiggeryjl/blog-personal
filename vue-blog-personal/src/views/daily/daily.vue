<script setup>
import { useRouter } from 'vue-router';
import { ref, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { ZoomIn, ZoomOut, ChatDotRound, Document } from '@element-plus/icons-vue';
import MyPagination from '@/components/MyPagination.vue';
import { getDailyListApi } from '@/api/daily.js';

const router = useRouter();

const dailyList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const loadFailed = ref(false);
let latestRequestId = 0;

const normalizeImages = (images) => {
  if (Array.isArray(images)) return images.filter(Boolean);
  if (typeof images !== 'string') return [];
  return images
    .split(',')
    .map((image) => image.trim())
    .filter(Boolean);
};

const getFileNameFromUrl = (url) => {
  try {
    const decoded = decodeURIComponent(String(url).split('?')[0]);
    return decoded.split('/').filter(Boolean).pop() || '附件';
  } catch {
    return String(url).split('/').filter(Boolean).pop() || '附件';
  }
};

const normalizeFiles = (files) => {
  const fileList = Array.isArray(files) ? files : typeof files === 'string' ? files.split(',') : [];
  return fileList
    .map((file) => (typeof file === 'string' ? file.trim() : file?.url))
    .filter(Boolean)
    .map((url) => ({ name: getFileNameFromUrl(url), url }));
};

const getDailyList = async () => {
  const requestId = ++latestRequestId;
  loading.value = true;
  try {
    const result = await getDailyListApi({
      page: currentPage.value,
      pageSize: pageSize.value,
    });
    if (requestId !== latestRequestId) return;

    if (result?.code !== 200) {
      dailyList.value = [];
      total.value = 0;
      loadFailed.value = true;
      ElMessage.error(result?.msg || '获取日常列表失败');
      return;
    }

    const rows = Array.isArray(result.data?.rows) ? result.data.rows : [];
    const nextTotal = Number(result.data?.total) || 0;
    const lastPage = Math.max(1, Math.ceil(nextTotal / pageSize.value));
    if (nextTotal > 0 && currentPage.value > lastPage) {
      total.value = nextTotal;
      currentPage.value = lastPage;
      return;
    }

    dailyList.value = rows.map((item) => ({
      ...item,
      images: normalizeImages(item.images),
      files: normalizeFiles(item.files),
      isLiked: false,
    }));
    total.value = nextTotal;
    loadFailed.value = false;
  } catch (error) {
    if (requestId !== latestRequestId) return;
    dailyList.value = [];
    loadFailed.value = true;
    console.error('获取日常列表异常', error);
  } finally {
    if (requestId === latestRequestId) {
      loading.value = false;
    }
  }
};

// 翻页 / 改变每页条数时重新拉取数据
watch([currentPage, pageSize], () => {
  getDailyList();
});

onMounted(() => {
  getDailyList();
});

// 点赞
const toggleLike = (item) => {
  item.isLiked = !item.isLiked;
  item.likeNum = (item.likeNum || 0) + (item.isLiked ? 1 : -1);
};

// 去评论页
const goComment = (id) => {
  router.push(`/daily/${id}`);
};

// ========== 图片预览（循环轮播 + 预加载 + loading） ==========
const showImageModal = ref(false);
const previewImageUrl = ref('');
const scale = ref(1);
const imageList = ref([]);
const currentImageIndex = ref(0);
const imageLoading = ref(false); // 加载状态

// 预加载单张图片
const preloadImage = (url) => {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(url);
    img.onerror = (err) => reject(err);
    img.src = url;
  });
};

// 预加载当前图片的相邻两张（循环）
const preloadAdjacent = (list, currentIdx) => {
  if (!list.length) return;
  const len = list.length;
  const prevIdx = (currentIdx - 1 + len) % len;
  const nextIdx = (currentIdx + 1) % len;
  if (list[prevIdx]) preloadImage(list[prevIdx]).catch(() => {});
  if (list[nextIdx]) preloadImage(list[nextIdx]).catch(() => {});
};

// 切换到指定索引的图片（带loading）
const switchToImage = async (newIndex) => {
  if (imageLoading.value) return;
  const len = imageList.value.length;
  if (len === 0) return;
  // 确保索引在合法范围（实际上调用前已处理循环，但防御一下）
  const safeIndex = (newIndex + len) % len;
  const targetUrl = imageList.value[safeIndex];
  if (!targetUrl) return;

  imageLoading.value = true;
  try {
    await preloadImage(targetUrl);
    currentImageIndex.value = safeIndex;
    previewImageUrl.value = targetUrl;
    scale.value = 1;
    // 预加载新的相邻图片
    preloadAdjacent(imageList.value, safeIndex);
  } catch (err) {
    console.warn('图片加载失败', err);
    currentImageIndex.value = safeIndex;
    previewImageUrl.value = targetUrl;
  } finally {
    imageLoading.value = false;
  }
};

// 点击图片打开预览（基于日常条目和图片索引）
const handleImageClick = async (item, idx) => {
  const imgs = item.images;
  if (!imgs.length) return;
  imageList.value = imgs;
  currentImageIndex.value = idx;
  previewImageUrl.value = imgs[idx];
  scale.value = 1;
  showImageModal.value = true;
  imageLoading.value = true;
  try {
    await preloadImage(imgs[idx]);
    preloadAdjacent(imgs, idx);
  } catch (err) {
    console.warn('图片加载失败', err);
  } finally {
    imageLoading.value = false;
  }
};

// 上一张（循环）
const prevImage = () => {
  if (imageLoading.value) return;
  const len = imageList.value.length;
  if (len === 0) return;
  const newIndex = (currentImageIndex.value - 1 + len) % len;
  switchToImage(newIndex);
};

// 下一张（循环）
const nextImage = () => {
  if (imageLoading.value) return;
  const len = imageList.value.length;
  if (len === 0) return;
  const newIndex = (currentImageIndex.value + 1) % len;
  switchToImage(newIndex);
};

// 放大/缩小
const zoomIn = () => {
  scale.value = Math.min(scale.value + 0.2, 3);
};
const zoomOut = () => {
  scale.value = Math.max(scale.value - 0.2, 0.6);
};

// 关闭弹窗
const closeModal = () => {
  showImageModal.value = false;
  previewImageUrl.value = '';
  scale.value = 1;
  imageLoading.value = false;
};
</script>

<template>
  <div class="common-article">
    <!-- 🔒 固定区域：标题 + 分割线 + 发布框（不受排版影响） -->
    <div class="fixed-header">
      <h2>日常</h2>
      <div class="publish-box" v-if="false">
        <!-- 你的发布输入框代码 -->
      </div>
      <div class="divider"></div>
    </div>

    <div v-loading="loading" class="daily-list">
      <div class="daily-item" v-for="item in dailyList" :key="item.id">
        <!-- 头像 -->
        <div class="card-header">
          <el-avatar :src="item.userAvatar" :size="42" />
          <div class="info">
            <div class="username">{{ item.userNickname }}</div>
            <div class="time">{{ item.publishTime || item.createTime }}</div>
          </div>
        </div>

        <div class="card-body">
          <!-- 文字内容 -->
          <div class="card-content">{{ item.content }}</div>

          <!-- 图片 -->
          <div class="card-images" v-if="item.images.length">
            <img
              v-for="(img, idx) in item.images"
              :key="`${item.id}-${idx}`"
              :src="img"
              @click.stop="handleImageClick(item, idx)"
              draggable="false"
              user-select="none"
            />
          </div>

          <!-- 附件 -->
          <div v-if="item.files.length" class="card-files">
            <el-link
              v-for="(file, idx) in item.files"
              :key="`${item.id}-file-${idx}`"
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
        </div>

        <!-- 点赞 + 评论 -->
        <div class="card-actions">
          <button class="like-btn" :class="{ active: item.isLiked }" @click.stop="toggleLike(item)">
            <font-awesome-icon icon="fa-solid fa-thumbs-up" /> {{ item.likeNum || 0 }}
          </button>
          <button class="comment-btn" @click.stop="goComment(item.id)">
            <el-icon>
              <ChatDotRound />
            </el-icon>
            {{ item.commentNum || 0 }}
          </button>
        </div>
      </div>

      <div v-if="!loading && loadFailed && dailyList.length === 0" class="empty-data">
        <span>日常数据加载失败，请稍后重试</span>
        <el-button type="primary" link @click="getDailyList">重新加载</el-button>
      </div>
      <div v-else-if="!loading && dailyList.length === 0" class="empty-data">暂无日常数据</div>
    </div>

    <MyPagination
      v-if="total > 0"
      :total="total"
      :current-page="currentPage"
      :page-size="pageSize"
      @update:current-page="currentPage = $event"
      @update:page-size="pageSize = $event"
    />

    <div v-if="showImageModal" class="image-modal" @click.self="closeModal">
      <!-- 关闭按钮 -->
      <div class="img-close" @click="closeModal">✕</div>

      <!-- 上一张 -->
      <div class="img-prev" @click="prevImage">‹</div>

      <div v-if="imageLoading" class="image-loading-overlay">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <!-- 图片 -->
      <img
        :src="previewImageUrl"
        alt="预览"
        class="preview-image"
        :style="{ transform: `scale(${scale})` }"
        @click.stop
        draggable="false"
        user-select="none"
      />

      <!-- 下一张 -->
      <div class="img-next" @click="nextImage">›</div>

      <!-- 缩放按钮 -->
      <div class="img-zoom">
        <div @click="zoomOut">
          <el-icon>
            <ZoomOut />
          </el-icon>
        </div>
        <div @click="zoomIn">
          <el-icon>
            <ZoomIn />
          </el-icon>
        </div>
        <!-- 图片计数器（循环时提示当前位置） -->
        <div class="image-counter" v-if="imageList.length">{{ currentImageIndex + 1 }} / {{ imageList.length }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-btn :deep(.el-icon) {
  font-size: 16px !important;
  margin-right: 4px;
}

.common-article {
  flex: 1;
  /* 占满剩余宽度 */
  padding: 5px 12px;
  border-radius: 8px;
  background-color: var(--card-bg);
}

/* 🔒 固定头部区域：永远保持原样 */
.fixed-header {
  margin-bottom: 20px;
}

.fixed-header h2 {
  margin: 0 0 8px;
  color: var(--text-color);
}

.divider {
  height: 1px;
  background: var(--border-color);
  margin-bottom: 20px;
}

.daily-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 120px;
}

.daily-item {
  background: var(--card-bg);
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 5px 16px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(127, 127, 127, 0.28);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 头像 */
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 2px;
}

.card-header .info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
}

.time {
  font-size: 12px;
  color: var(--text-secondary-color);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
  padding: 14px;
  border-radius: 8px;
  background: var(--card-secound-bg);
  box-shadow: inset 0 0 0 1px rgba(127, 127, 127, 0.18), 0 2px 8px rgba(15, 23, 42, 0.04);
}

/* 文字内容 */
.card-content {
  color: var(--text-main-color);
  font-size: 15px;
  line-height: 1.6;
  overflow-wrap: anywhere;
  word-break: break-word;
}

/* 图片网格 */
.card-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 8px;
}

.card-images img {
  width: 100%;
  height: 90px;
  object-fit: cover;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.2s;
}

.card-images img:hover {
  transform: scale(1.03);
}

.card-files {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
  min-width: 0;
  padding-top: 12px;
  border-top: 1px solid rgba(127, 127, 127, 0.24);
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

/* 操作栏 */
.card-actions {
  display: flex;
  justify-content: flex-start;
  gap: 14px;
  padding: 0 2px;
}

.like-btn,
.comment-btn {
  background: transparent;
  border: none;
  font-size: 13px;
  color: var(--text-secondary-color);
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 6px;
}

.like-btn:hover,
.comment-btn:hover {
  background: var(--card-bg-hover);
}

.like-btn.active {
  color: #ff4d64;
}

.empty-data {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-secondary-color);
  font-size: 16px;
  padding: 60px 0;
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  cursor: zoom-out;
}

/* 预览图片 */
.preview-image {
  max-width: 85%;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 8px;
  cursor: default;
  transition: transform 0.2s ease;
  user-select: none !important;
  -webkit-user-select: none !important;
  -webkit-user-drag: none !important;
  outline: none !important;
  border: none !important;
}

/* 关闭按钮 */
.img-close {
  position: absolute;
  top: 30px;
  right: 40px;
  font-size: 28px;
  color: #fff;
  cursor: pointer;
  user-select: none;
  z-index: 10;
}

.img-close:hover {
  color: #ff4444;
}

/* 上一张 / 下一张 */
.img-prev,
.img-next {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 50px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  user-select: none;
  z-index: 10;
  padding: 0 20px;
}

.img-prev {
  left: 20px;
}

.img-next {
  right: 20px;
}

.img-prev:hover,
.img-next:hover {
  color: #fff;
}

/* 图片加载遮罩 */
.image-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10001;
  color: white;
  font-size: 16px;
  backdrop-filter: blur(2px);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 缩放按钮 */
.img-zoom {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  align-items: center;
  z-index: 10;
}

/* 缩放按钮（圆形） */
.img-zoom div:not(.image-counter) {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}

.img-zoom div:not(.image-counter):hover {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(1.05);
}

/* 计数器样式 - 独立矩形，与按钮间隔开 */
.image-counter {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  padding: 8px 14px;
  border-radius: 30px;
  font-size: 14px;
  font-family: monospace;
  color: white;
  margin-left: 8px;
  /* 与缩放按钮拉开距离 */
  pointer-events: none;
  white-space: nowrap;
}

:deep(img) {
  user-select: none !important;
  -webkit-user-select: none !important;
  pointer-events: auto;
}

@media (max-width: 600px) {
  .daily-item {
    gap: 12px;
    padding: 12px;
  }

  .card-body {
    gap: 10px;
    padding: 12px;
  }
}
</style>

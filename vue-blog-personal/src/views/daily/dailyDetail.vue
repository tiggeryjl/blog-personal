<script setup>
import { useRoute } from 'vue-router';
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import Emoji from '@/components/Emoji.vue';
import CommentList from '@/components/Comment.vue';
import { ZoomIn, ZoomOut, ChatDotRound, DArrowLeft, Document, View } from '@element-plus/icons-vue';
import { addDailyViewApi, getDailyDetailApi } from '@/api/daily';
import { addCommentReplyApi, addDailyCommentApi, getDailyCommentListApi } from '@/api/comment.js';
import { likeApi, LIKE_TARGET_TYPE } from '@/api/like.js';
import { requireLogin } from '@/utils/auth';
import { isGuestLiked, rememberGuestLiked } from '@/utils/guestLike.js';
import { useUserStore } from '@/store/userloginstatus';

const route = useRoute();
const userStore = useUserStore();
const isLogin = computed(() => !!userStore.user_token);

const dailyItem = ref(null);
const detailLoading = ref(false);
const detailMessage = ref('');
const detailLoadFailed = ref(false);
let latestDetailRequestId = 0;
let componentUnmounted = false;

const isCurrentDaily = (id) => !componentUnmounted && String(route.params.id) === String(id);

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

const fetchDailyDetail = async (id) => {
  const requestId = ++latestDetailRequestId;
  dailyItem.value = null;
  detailLoading.value = true;
  detailMessage.value = '';
  detailLoadFailed.value = false;

  try {
    const result = await getDailyDetailApi(id);
    if (requestId !== latestDetailRequestId) return false;

    if (result?.code !== 200 || !result.data) {
      detailMessage.value = result?.msg || '日常不存在或暂未公开';
      return false;
    }

    dailyItem.value = {
      ...result.data,
      images: normalizeImages(result.data.images),
      files: normalizeFiles(result.data.files),
      isLiked:
        result.data.liked === true ||
        (!isLogin.value && isGuestLiked(LIKE_TARGET_TYPE.DAILY, result.data.id)),
    };
    return true;
  } catch (error) {
    if (requestId !== latestDetailRequestId) return false;
    detailLoadFailed.value = true;
    detailMessage.value = '日常数据加载失败，请稍后重试';
    console.error('获取日常详情异常', error);
    return false;
  } finally {
    if (requestId === latestDetailRequestId) {
      detailLoading.value = false;
    }
  }
};

const loadDailyDetail = async (id) => {
  const loaded = await fetchDailyDetail(id);
  if (loaded && String(route.params.id) === String(id)) {
    // 浏览数 +1（失败静默，不影响详情展示）
    addDailyViewApi(id).catch(() => {});
  }
};

// 点赞日常
const likingDailyIds = new Set();
const toggleLike = async (item) => {
  if (item.isLiked) {
    ElMessage.warning('你已经点过赞了');
    return;
  }
  const dailyId = item.id;
  const key = String(dailyId);
  if (likingDailyIds.has(key)) return;
  likingDailyIds.add(key);
  try {
    const result = await likeApi({ targetType: LIKE_TARGET_TYPE.DAILY, targetId: dailyId });
    if (!isCurrentDaily(dailyId)) return;
    if (result?.code === 200) {
      const data = result.data || {};
      item.isLiked = data.liked !== false;
      if (typeof data.likeCount === 'number') item.likeNum = data.likeCount;
      if (!isLogin.value) rememberGuestLiked(LIKE_TARGET_TYPE.DAILY, dailyId);
      // ElMessage.success('点赞成功！');
    } else {
      ElMessage.error(result?.msg || '点赞失败，请稍后重试');
    }
  } catch (error) {
    if (isCurrentDaily(dailyId)) {
      ElMessage.error('点赞失败，请稍后重试');
    }
  } finally {
    likingDailyIds.delete(key);
  }
};

// ========== 图片预览 ==========
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

// 评论点赞中的目标集合，防止连点
const likingTargets = new Set();
const handleCommentLike = async (targetId, onSuccess) => {
  const dailyId = route.params.id;
  const likeKey = `${LIKE_TARGET_TYPE.COMMENT}_${targetId}`;
  if (likingTargets.has(likeKey)) return;
  likingTargets.add(likeKey);
  try {
    const result = await likeApi({ targetType: LIKE_TARGET_TYPE.COMMENT, targetId });
    if (!isCurrentDaily(dailyId)) return;
    if (result.code === 200) {
      const data = result.data || {};
      onSuccess(data);
      // ElMessage.success(data.liked ? '点赞成功！' : '已取消点赞');
    } else {
      ElMessage.error(result.msg || '操作失败，请稍后重试');
    }
  } catch (error) {
    if (isCurrentDaily(dailyId)) {
      ElMessage.error('点赞失败，请稍后重试');
    }
  } finally {
    likingTargets.delete(likeKey);
  }
};

// 点赞主评论
const likeComment = (commentId) => {
  const comment = currentCommentList.value.find((item) => item.id === commentId);
  if (!comment) return;
  if (comment.liked) {
    ElMessage.warning('你已经点过赞了');
    return;
  }
  handleCommentLike(commentId, (data) => {
    comment.liked = data.liked !== false;
    if (typeof data.likeCount === 'number') comment.likeNum = data.likeCount;
    if (!isLogin.value) rememberGuestLiked(LIKE_TARGET_TYPE.COMMENT, commentId);
  });
};

// 点赞回复
const likeReply = (commentId, replyId) => {
  const comment = currentCommentList.value.find((item) => item.id === commentId);
  if (!comment) return;
  const reply = comment.replies.find((item) => item.id === replyId);
  if (!reply) return;
  if (reply.liked) {
    ElMessage.warning('你已经点过赞了');
    return;
  }
  handleCommentLike(replyId, (data) => {
    reply.liked = data.liked !== false;
    if (typeof data.likeCount === 'number') reply.likeNum = data.likeCount;
    if (!isLogin.value) rememberGuestLiked(LIKE_TARGET_TYPE.COMMENT, replyId);
  });
};

const currentCommentList = ref([]);
const commentLoading = ref(false);
const commentLoadFailed = ref(false);
let latestCommentRequestId = 0;

// 游客未登录时后端不回传 liked，用本地记录补齐已点赞状态
const applyGuestLikedState = (list) => {
  if (!Array.isArray(list) || isLogin.value) return;
  list.forEach((item) => {
    if (isGuestLiked(LIKE_TARGET_TYPE.COMMENT, item.id)) {
      item.liked = true;
    }
    applyGuestLikedState(item.replies);
  });
};

const getCommentList = async (id, reset = false) => {
  const requestId = ++latestCommentRequestId;
  if (reset) currentCommentList.value = [];
  commentLoading.value = true;
  commentLoadFailed.value = false;
  try {
    const result = await getDailyCommentListApi(id);
    if (requestId !== latestCommentRequestId) return false;
    if (result?.code === 200) {
      currentCommentList.value = Array.isArray(result.data) ? result.data : [];
      applyGuestLikedState(currentCommentList.value);
      if (dailyItem.value && String(dailyItem.value.id) === String(id)) {
        dailyItem.value.commentNum = countComments(currentCommentList.value);
      }
      return true;
    }
    commentLoadFailed.value = true;
    ElMessage.error(result?.msg || '获取评论失败，请稍后重试');
    return false;
  } catch (error) {
    if (requestId === latestCommentRequestId) {
      commentLoadFailed.value = true;
      ElMessage.error('获取评论失败，请稍后重试');
    }
    return false;
  } finally {
    if (requestId === latestCommentRequestId) {
      commentLoading.value = false;
    }
  }
};

const countComments = (list) => {
  if (!list) return 0;
  return list.reduce((sum, item) => sum + 1 + countComments(item.replies), 0);
};
const commentTotal = computed(() => countComments(currentCommentList.value));

const commentForm = ref({ content: '' });
const publishing = ref(false);
let latestPublishRequestId = 0;

// 发表评论/回复评论
const publishComment = async (commentId, replyId, content) => {
  if (!requireLogin('登录后才能发表评论哦~')) return false;
  if (commentId === undefined && replyId === undefined && content === undefined) {
    const draftContent = commentForm.value.content;
    const submittedContent = draftContent.trim();
    if (!submittedContent) {
      ElMessage.warning('评论内容不能为空~');
      return false;
    }
    const dailyId = dailyItem.value?.id;
    if (!dailyId || publishing.value) return false;
    const requestId = ++latestPublishRequestId;
    publishing.value = true;
    try {
      const result = await addDailyCommentApi(dailyId, submittedContent);
      if (requestId !== latestPublishRequestId || !isCurrentDaily(dailyId)) {
        return false;
      }
      if (result?.code === 200) {
        if (commentForm.value.content === draftContent) {
          commentForm.value.content = '';
        }
        ElMessage.success('评论发布成功！');
        await getCommentList(dailyId);
        return true;
      }
      ElMessage.error(result?.msg || '评论发布失败，请稍后重试');
      return false;
    } catch (error) {
      if (requestId === latestPublishRequestId && isCurrentDaily(dailyId)) {
        ElMessage.error('评论发布失败，请稍后重试');
      }
      return false;
    } finally {
      if (requestId === latestPublishRequestId) {
        publishing.value = false;
      }
    }
  }

  if (!content || !content.trim()) {
    ElMessage.warning('回复内容不能为空');
    return false;
  }
  const parentId = replyId || commentId;
  const dailyId = dailyItem.value?.id;
  if (!parentId || !dailyId) return false;
  try {
    const result = await addCommentReplyApi({
      parentId,
      content: content.trim(),
    });
    if (!isCurrentDaily(dailyId)) return false;
    if (result?.code === 200) {
      ElMessage.success('回复成功！');
      await getCommentList(dailyId);
      return true;
    }
    ElMessage.error(result?.msg || '回复失败，请稍后重试');
    return false;
  } catch (error) {
    if (isCurrentDaily(dailyId)) {
      ElMessage.error('回复失败，请稍后重试');
    }
    return false;
  }
};

// 表情
const showEmoji = ref(false);
const closeEmojiOutside = (e) => {
  const emojiBox = document.querySelector('.emoji-panel');
  const emojiBtn = document.querySelector('.emoji-btn');
  if (emojiBox && !emojiBox.contains(e.target) && !emojiBtn.contains(e.target)) {
    showEmoji.value = false;
  }
};
const insertEmoji = (code) => {
  commentForm.value.content += code;
  closeEmoji();
};
const closeEmoji = () => {
  showEmoji.value = false;
};

// 路由切换刷新
watch(
  () => route.params.id,
  (id) => {
    latestPublishRequestId += 1;
    publishing.value = false;
    commentForm.value.content = '';
    closeModal();
    loadDailyDetail(id);
    getCommentList(id, true);
  },
  { immediate: true }
);

onMounted(() => {
  document.addEventListener('click', closeEmojiOutside);
});

onUnmounted(() => {
  componentUnmounted = true;
  latestDetailRequestId += 1;
  latestCommentRequestId += 1;
  latestPublishRequestId += 1;
  document.removeEventListener('click', closeEmojiOutside);
});
</script>

<template>
  <div class="common-article-detail">
    <div v-loading="detailLoading" class="article-detail">
      <button class="button-return" @click="$router.go(-1)">
        <el-icon> <DArrowLeft /> </el-icon>返回
      </button>
      <div class="divider"></div>

      <div class="daily-item" v-if="dailyItem">
        <div class="card-header">
          <el-avatar :src="dailyItem.userAvatar" :size="50" />
          <div class="info">
            <div class="username">{{ dailyItem.userNickname }}</div>
            <div class="time">{{ dailyItem.publishTime || dailyItem.createTime }}</div>
          </div>
        </div>

        <div class="card-content">{{ dailyItem.content }}</div>

        <div class="card-images" v-if="dailyItem.images.length">
          <img
            v-for="(img, idx) in dailyItem.images"
            :key="`${dailyItem.id}-${idx}`"
            :src="img"
            @click.stop="handleImageClick(dailyItem, idx)"
            draggable="false"
          />
        </div>

        <div v-if="dailyItem.files.length" class="card-files">
          <el-link
            v-for="(file, idx) in dailyItem.files"
            :key="`${dailyItem.id}-file-${idx}`"
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

        <div class="card-actions">
          <span
            ><el-icon>
              <View />
            </el-icon>
            {{ dailyItem.viewNum }}</span
          >
          <button class="like-btn" :class="{ active: dailyItem.isLiked }" @click.stop="toggleLike(dailyItem)">
            <font-awesome-icon icon="fa-solid fa-thumbs-up" /> {{ dailyItem.likeNum || 0 }}
          </button>
          <button class="comment-btn">
            <el-icon>
              <ChatDotRound />
            </el-icon>
            {{ dailyItem.commentNum || 0 }}
          </button>
        </div>
      </div>

      <div v-else-if="!detailLoading" class="empty-data">
        <span>{{ detailMessage || '暂无日常数据' }}</span>
        <el-button v-if="detailLoadFailed" type="primary" link @click="loadDailyDetail(route.params.id)">
          重新加载
        </el-button>
      </div>

      <div v-if="dailyItem" v-loading="commentLoading" class="comment-section">
        <div class="comment-publish-box">
          <h3>留下你的想法~</h3>
          <div class="publish-form">
            <div class="comment-textarea-container">
              <textarea
                v-model="commentForm.content"
                :placeholder="isLogin ? '请输入评论内容...' : '登录后即可发表评论~'"
                maxlength="500"
                class="comment-textarea"
              ></textarea>
              <div class="emoji-btn" @click.stop="showEmoji = !showEmoji">
                <font-awesome-icon :icon="['fa', 'face-smile']" />
              </div>
              <Emoji v-if="showEmoji" :show="showEmoji" @select="insertEmoji" @close="closeEmoji" />
              <el-button
                type="primary"
                size="small"
                :loading="publishing"
                @click="publishComment()"
                class="publish-btn"
              >
                发表评论
              </el-button>
            </div>
          </div>
        </div>

        <h3>
          评论
          <span>({{ commentTotal }})</span>
        </h3>
        <div v-if="commentLoadFailed" class="comment-load-error">
          <span>评论加载失败</span>
          <el-button type="primary" link @click="getCommentList(route.params.id)">重新加载</el-button>
        </div>
        <CommentList
          v-if="!commentLoadFailed || currentCommentList.length > 0"
          :comment-list="currentCommentList"
          :on-send-reply="publishComment"
          @like-comment="likeComment"
          @like-reply="likeReply"
        />
      </div>
    </div>

    <div v-if="showImageModal" class="image-modal" @click.self="closeModal">
      <div class="img-close" @click="closeModal">✕</div>
      <div class="img-prev" @click="prevImage">‹</div>
      <div v-if="imageLoading" class="image-loading-overlay">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <img :src="previewImageUrl" class="preview-image" :style="{ transform: `scale(${scale})` }" draggable="false" />
      <div class="img-next" @click="nextImage">›</div>
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
.common-article-detail {
  display: flex;
  gap: 30px;
  min-height: calc(100vh - 65px);
  width: 82%;
  margin: 0 auto;
}

.article-detail {
  background-color: var(--card-bg);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  border-radius: 8px;
  padding: 20px 12px;
}

.button-return {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 80px;
  height: 36px;
  background-color: var(--primary-color);
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
  border-radius: 18px;
  border: 1px solid var(--primary-color);
}

.button-return:hover {
  background-color: #ebb1a2b8;
  color: var(--hover-color);
  border: 1px solid #ebb1a2b8;
}

.divider {
  height: 1px;
  background: var(--border-color);
}

.daily-item {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header .info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-color);
}

.time {
  font-size: 14px;
  color: var(--text-secondary-color);
}

.card-content {
  color: var(--text-main-color);
  font-size: 17px;
  line-height: 1.6;
  word-break: break-all;
}

.card-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 8px;
}

.card-images img {
  width: 100%;
  height: 150px;
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
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
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

.card-actions {
  display: flex;
  justify-content: flex-start;
  gap: 14px;
  padding-top: 4px;
}

.like-btn,
.comment-btn {
  background: transparent;
  border: none;
  font-size: 15px;
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

.comment-section {
  margin-top: 20px;
}

.comment-load-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 56px;
  color: var(--text-secondary-color);
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

.preview-image {
  max-width: 85%;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 8px;
  cursor: default;
  transition: transform 0.2s ease;
  user-select: none !important;
}

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

/* 发表评论 */
.comment-publish-box {
  margin-bottom: 30px;
}

.comment-publish-box h3 {
  font-size: 18px;
  color: var(--text-color);
  margin-bottom: 16px;
  position: relative;
  padding-left: 14px;
}

.comment-publish-box h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 4px;
  height: 18px;
  background: var(--primary-color);
  border-radius: 2px;
}

.publish-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-nickname {
  width: 220px;
}

/* 模拟输入框容器，完全控制边框和内边距 */
.comment-textarea-container {
  position: relative;
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--card-bg);
  padding: 12px 0px 0px 12px;
  box-sizing: border-box;
}

/* 原生 textarea，去掉默认边框 */
.comment-textarea {
  width: 100%;
  min-height: 120px;
  border: none;
  outline: none;
  background: transparent;
  resize: vertical;
  padding-right: 100px;
  padding-bottom: 40px;
  box-sizing: border-box;
  color: var(--text-main-color);
  font-size: 15px;
  line-height: 1.5;
}

.comment-textarea::placeholder {
  color: var(--text-secondary-color);
}

/* 表情按钮 */
.emoji-btn {
  position: absolute;
  left: 12px;
  bottom: 12px;
  cursor: pointer;
  z-index: 2;
  transition: transform 0.2s;
}

.emoji-btn :deep(.svg-inline--fa) {
  color: #313131 !important;
  font-size: 17px !important;
}

.emoji-btn:hover :deep(.svg-inline--fa) {
  transform: scale(1.1);
  color: var(--primary-color) !important;
}

/* 表情面板 */
.emoji-panel {
  position: absolute;
  left: 12px;
  bottom: 46px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 10px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  max-width: 280px;
  max-height: 160px;
  overflow-y: auto;
  z-index: 99;
}

.emoji-panel span {
  position: relative;
  font-size: 18px;
  cursor: pointer;
  padding: 2px;
}

.emoji-panel span:hover {
  transform: scale(1.2);
}

.emoji-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: #444;
  color: #fff;
  font-size: 12px;
  padding: 3px 6px;
  border-radius: 4px;
  white-space: nowrap;
  pointer-events: none;
  margin-bottom: 4px;
  z-index: 100;
}

.emoji-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 3px;
  border-style: solid;
  border-color: #444 transparent transparent transparent;
}

/* 按钮定位在容器右下角 */
.publish-btn {
  position: absolute;
  font-size: 14px;
  right: 18px;
  bottom: 12px;
  z-index: 1;
}

.publish-btn:hover {
  background-color: #277ec1;
}
</style>

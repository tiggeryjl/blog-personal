<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { DArrowLeft, DArrowRight, ZoomIn, ZoomOut } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import CommentList from '@/components/Comment.vue';
import Emoji from '@/components/Emoji.vue';
import { getArticleDetailApi } from '@/api/article.js';
import { getArticleCommentListApi, addArticleCommentApi, addCommentReplyApi } from '@/api/comment.js';
import { likeApi, LIKE_TARGET_TYPE } from '@/api/like.js';

const router = useRouter();
const route = useRoute();

const article = ref({
  id: '',
  title: '',
  summary: '',
  content: '',
  userNickname: '',
  createTime: '',
  category: '',
  tags: [],
  viewNum: 0,
  likeNum: 0,
  commentNum: 0,
});

const prevArticle = ref({});
const nextArticle = ref({});
// 当前用户是否已点赞文章
const articleLiked = ref(false);
//根据id获取当前文章信息
const getArticle = async () => {
  try {
    const articleId = route.params.id;
    articleLiked.value = false;
    const result = await getArticleDetailApi(articleId);

    if (result.code === 200) {
      article.value = result.data.articleVo || {};
      prevArticle.value = result.data.prevArticle || {};
      nextArticle.value = result.data.nextArticle || {};
      articleLiked.value = result.data.liked === true;
      getCommentList(article.value.id);
    }
  } catch (error) {
    ElMessage.error('加载文章详情失败，请稍后重试');
  }
};

// 上一页下一页跳转
const goToArticle = (id) => {
  router.push(`/article/${id}`);
};

// 点赞中目标集合，防止连点
const likingTargets = new Set();

// 统一调用点赞接口并更新本地数据
const handleLike = async (targetType, targetId, onSuccess) => {
  const likeKey = `${targetType}_${targetId}`;
  if (likingTargets.has(likeKey)) return;
  likingTargets.add(likeKey);
  try {
    const result = await likeApi({ targetType, targetId });
    if (result.code === 200) {
      const data = result.data || {};
      onSuccess(data);
      ElMessage.success(data.liked ? '点赞成功！' : '已取消点赞');
    } else {
      ElMessage.error(result.msg || '操作失败，请稍后重试');
    }
  } catch (error) {
    ElMessage.error('点赞失败，请稍后重试');
  } finally {
    likingTargets.delete(likeKey);
  }
};

// 点赞主评论
const likeComment = (commentId) => {
  const comment = currentCommentList.value.find((c) => c.id === commentId);
  if (!comment) return;
  if (comment.liked) {
    ElMessage.warning('你已经点过赞了');
    return;
  }
  handleLike(LIKE_TARGET_TYPE.COMMENT, commentId, (data) => {
    comment.liked = data.liked === true;
    if (typeof data.likeCount === 'number') comment.likeNum = data.likeCount;
  });
};

// 点赞回复
const likeReply = (commentId, replyId) => {
  const comment = currentCommentList.value.find((c) => c.id === commentId);
  if (!comment) return;
  const reply = comment.replies.find((r) => r.id === replyId);
  if (!reply) return;
  if (reply.liked) {
    ElMessage.warning('你已经点过赞了');
    return;
  }
  handleLike(LIKE_TARGET_TYPE.COMMENT, replyId, (data) => {
    reply.liked = data.liked === true;
    if (typeof data.likeCount === 'number') reply.likeNum = data.likeCount;
  });
};

// 当前文章的评论列表
const currentCommentList = ref([]);

const getCommentList = async (id) => {
  currentCommentList.value = [];
  try {
    const result = await getArticleCommentListApi(id);
    if (result.code === 200) {
      currentCommentList.value = result.data;
    } else {
      ElMessage.error('获取评论失败，请稍后重新尝试或者联系博主反馈，感谢！');
    }
  } catch (err) {
    ElMessage.error('获取评论失败，接口异常，感谢！');
  }
};

// 递归统计评论总数
const countComments = (list) => {
  if (!list) return 0;
  return list.reduce((sum, item) => sum + 1 + countComments(item.replies), 0);
};
const commentTotal = computed(() => countComments(currentCommentList.value));

// 评论表单
const commentForm = ref({ content: '' });
const publishing = ref(false);

// 发表评论/回复评论
const publishComment = async (commentId, replyId, content) => {
  // 发布新评论
  if (commentId === undefined && replyId === undefined && content === undefined) {
    if (!commentForm.value.content.trim()) {
      ElMessage.warning('评论内容不能为空~');
      return false;
    }
    if (publishing.value) return false;
    publishing.value = true;
    try {
      const result = await addArticleCommentApi(article.value.id, commentForm.value.content.trim());
      if (result.code === 200) {
        commentForm.value.content = '';
        ElMessage.success('评论发布成功！');
        await getCommentList(article.value.id);
        return true;
      }
      ElMessage.error(result.msg || '评论发布失败，请稍后重试');
      return false;
    } catch (error) {
      ElMessage.error('评论发布失败，请稍后重试');
      return false;
    } finally {
      publishing.value = false;
    }
  }

  // 回复评论
  if (!content || !content.trim()) {
    ElMessage.warning('回复内容不能为空');
    return false;
  }
  const parentId = replyId || commentId;
  if (!parentId) return false;
  try {
    const result = await addCommentReplyApi({
      parentId,
      content: content.trim(),
    });
    if (result.code === 200) {
      ElMessage.success('回复成功！');
      await getCommentList(article.value.id);
      return true;
    }
    ElMessage.error(result.msg || '回复失败，请稍后重试');
    return false;
  } catch (error) {
    ElMessage.error('回复失败，请稍后重试');
    return false;
  }
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

// 点击图片打开预览
const handleImageClick = async (e) => {
  if (e.target.tagName !== 'IMG') return;
  const imgs = document.querySelectorAll('.article-body img');
  imageList.value = Array.from(imgs).map((img) => img.src);
  const clickedIndex = Array.from(imgs).findIndex((img) => img === e.target);
  if (clickedIndex === -1) return;

  currentImageIndex.value = clickedIndex;
  previewImageUrl.value = imageList.value[clickedIndex];
  scale.value = 1;
  showImageModal.value = true;
  imageLoading.value = true;
  try {
    await preloadImage(imageList.value[clickedIndex]);
    preloadAdjacent(imageList.value, clickedIndex);
  } catch (err) {
    console.warn('初始图片加载失败', err);
  } finally {
    imageLoading.value = false;
  }
};

// 上一张
const prevImage = () => {
  if (imageLoading.value) return;
  const len = imageList.value.length;
  if (len === 0) return;
  const newIndex = (currentImageIndex.value - 1 + len) % len;
  switchToImage(newIndex);
};

// 下一张
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

//点赞文章
const likeArticle = () => {
  if (!article.value.id) return;
  if (articleLiked.value) {
    ElMessage.warning('你已经点过赞了');
    return;
  }
  handleLike(LIKE_TARGET_TYPE.ARTICLE, article.value.id, (data) => {
    articleLiked.value = data.liked === true;
    if (typeof data.likeCount === 'number') article.value.likeNum = data.likeCount;
  });
};

// 表情相关
const showEmoji = ref(false);
const closeEmojiOutside = (e) => {
  const emojiBox = document.querySelector('.emoji-panel');
  const emojiBtn = document.querySelector('.emoji-btn');

  if (emojiBox && !emojiBox.contains(e.target) && !emojiBtn.contains(e.target)) {
    showEmoji.value = false;
    // hoverEmoji.value = null
  }
};

const insertEmoji = (code) => {
  if (showEmoji.value) {
    commentForm.value.content += code;
    closeEmoji();
  }
};
const closeEmoji = () => {
  showEmoji.value = false;
};

watch(
  () => route.params.id,
  () => {
    getArticle();
    commentForm.value.content = '';
  }
);

onMounted(() => {
  getArticle();
  document.addEventListener('click', closeEmojiOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', closeEmojiOutside);
});
</script>
<template>
  <div class="common-article-detail">
    <div class="article-detail">
      <button class="button-return" @click="$router.go(-1)">
        <el-icon> <DArrowLeft /> </el-icon>返回
      </button>
      <div class="divider"></div>

      <!-- 文章头部 -->
      <div class="article-header">
        <h1 class="title">{{ article.title }}</h1>
        <div class="meta">
          <span>作者：{{ article.userNickname }}</span>
          <span>{{ article.createTime }}</span>
          <span>分类：{{ article.category }}</span>
          <span>浏览 {{ article.viewNum }}</span>
        </div>
      </div>

      <!-- 文章正文 -->
      <div class="content">
        <div class="article-body" v-html="article.content" @click="handleImageClick"></div>
      </div>

      <div class="article-actions">
        <el-button type="primary" :type="articleLiked ? 'success' : 'primary'" @click="likeArticle">
          <font-awesome-icon icon="fa-solid fa-thumbs-up" />
          {{ articleLiked ? '已点赞' : '点赞' }} {{ article.likeNum }}
        </el-button>
      </div>

      <div class="article-pagination">
        <button v-if="prevArticle.id" class="prev-btn" @click="goToArticle(prevArticle.id)">
          <el-icon>
            <DArrowLeft />
          </el-icon>
          上一章：{{ prevArticle.title }}
        </button>

        <button v-if="nextArticle.id" class="next-btn" @click="goToArticle(nextArticle.id)">
          下一章：{{ nextArticle.title }}
          <el-icon>
            <DArrowRight />
          </el-icon>
        </button>
      </div>

      <div class="card">
        <!-- 发表评论 -->
        <div class="comment-publish-box">
          <h3>留下你的想法~</h3>
          <div class="publish-form">
            <div class="comment-textarea-container">
              <textarea
                v-model="commentForm.content"
                placeholder="请输入评论内容..."
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
        <CommentList
          :comment-list="currentCommentList"
          :on-send-reply="publishComment"
          @like-comment="likeComment"
          @like-reply="likeReply"
        />
      </div>
    </div>

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
.common-article-detail {
  color: #ffffff;
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
  /*按钮内部变成弹性布局 */
  align-items: center;
  /*垂直居中*/
  justify-content: center;
  /*水平居中*/
  gap: 4px;
  /*文字和符号加点间距 */
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
  margin-bottom: 20px;
}

.article-header {
  margin-bottom: 30px;
  text-align: center;
}

.title {
  font-size: 28px;
  font-weight: 500;
  color: var(--text-color);
  margin-bottom: 12px;
}

.meta {
  color: var(--text-secondary-color);
  font-size: 14px;
  display: flex;
  gap: 16px;
  justify-content: center;
}

.article-body {
  color: var(--text-main-color);
  font-size: 16px;
  line-height: 1.8;
  word-break: break-all;
}

.article-body :deep(p) {
  text-indent: 2em;
  margin: 0 0 1.2em 0;
  text-align: justify;
}

.article-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 12px auto;
  display: block;
  cursor: pointer;
  border: 2px solid var(--border-color);
  user-select: none !important;
  -webkit-user-select: none !important;
  -webkit-user-drag: none !important;
  outline: none !important;
}

.article-body :deep(img:hover) {
  border: 2px solid var(--hover-color);
}

.article-body :deep(h1),
.article-body :deep(h2),
.article-body :deep(h3) {
  color: var(--text-color);
  margin: 1.2em 0 0.6em 0;
  text-align: center;
}

.article-actions {
  margin-top: 30px;
  text-align: center;
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

img {
  user-select: none !important;
  -webkit-user-select: none !important;
  -webkit-user-drag: none !important;
  pointer-events: auto;
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

/* 翻页按钮 */
.article-pagination {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  padding: 20px;
  border-top: 1px dashed var(--border-color);
  border-bottom: 1px dashed var(--border-color);
}

/* 左边按钮 */
.prev-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-color);
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  transition: 0.2s;
}

/* 右边按钮 */
.next-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-color);
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  transition: 0.2s;
  margin-left: auto;
}

.prev-btn:hover,
.next-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
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

.card {
  padding: 0px 24px;
}

.card h3 {
  font-size: 18px;
  color: var(--text-color);
  margin-bottom: 16px;
  position: relative;
  padding-left: 14px;
}

.card h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 4px;
  height: 18px;
  background: var(--primary-color);
  border-radius: 2px;
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

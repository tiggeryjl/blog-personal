<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import Emoji from '@/components/Emoji.vue'
import CommentList from '@/components/Comment.vue'
import { ZoomIn, ZoomOut, ChatDotRound, DArrowLeft } from '@element-plus/icons-vue'
import { addDailyViewApi } from '@/api/daily'

const router = useRouter()
const route = useRoute()

const dailyItem = ref(null)

const fetchDailyDetail = () => {
  const id = route.params.id
  const mockList = [
    {
      id: 1,
      userId: 101,
      avatar: 'https://picsum.photos/64/64',
      username: '张三',
      content: '今天天气不错，分享一张随手拍～',
      images: [
        'https://picsum.photos/600/400?random=1',
        'https://picsum.photos/600/400?random=2'
      ],
      createTime: '2025-12-20 15:30',
      like: 23,
      comment: 5,
      isLiked: false
    },
    {
      id: 2,
      userId: 102,
      avatar: 'https://picsum.photos/64/64?random=2',
      username: '李四',
      content: '记录一下今天的学习日常：Vue3 + ElementPlus 开发中',
      images: [],
      createTime: '2025-12-20 14:10',
      like: 12,
      comment: 3,
      isLiked: false
    }
  ]
  dailyItem.value = mockList.find(item => item.id == id)
}

// 点赞
const toggleLike = (item) => {
  item.isLiked = !item.isLiked
  item.like += item.isLiked ? 1 : -1
}

// ========== 图片预览（循环轮播 + 预加载 + loading） ==========
const showImageModal = ref(false)
const previewImageUrl = ref('')
const scale = ref(1)
const imageList = ref([])
const currentImageIndex = ref(0)
const imageLoading = ref(false)   // 加载状态

// 预加载单张图片
const preloadImage = (url) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(url)
    img.onerror = (err) => reject(err)
    img.src = url
  })
}

// 预加载当前图片的相邻两张（循环）
const preloadAdjacent = (list, currentIdx) => {
  if (!list.length) return
  const len = list.length
  const prevIdx = (currentIdx - 1 + len) % len
  const nextIdx = (currentIdx + 1) % len
  if (list[prevIdx]) preloadImage(list[prevIdx]).catch(() => { })
  if (list[nextIdx]) preloadImage(list[nextIdx]).catch(() => { })
}

// 切换到指定索引的图片（带loading）
const switchToImage = async (newIndex) => {
  if (imageLoading.value) return
  const len = imageList.value.length
  if (len === 0) return
  // 确保索引在合法范围（实际上调用前已处理循环，但防御一下）
  const safeIndex = (newIndex + len) % len
  const targetUrl = imageList.value[safeIndex]
  if (!targetUrl) return

  imageLoading.value = true
  try {
    await preloadImage(targetUrl)
    currentImageIndex.value = safeIndex
    previewImageUrl.value = targetUrl
    scale.value = 1
    // 预加载新的相邻图片
    preloadAdjacent(imageList.value, safeIndex)
  } catch (err) {
    console.warn('图片加载失败', err)
    currentImageIndex.value = safeIndex
    previewImageUrl.value = targetUrl
  } finally {
    imageLoading.value = false
  }
}

// 点击图片打开预览（基于日常条目和图片索引）
const handleImageClick = async (item, idx) => {
  const imgs = item.images
  if (!imgs.length) return
  imageList.value = imgs
  currentImageIndex.value = idx
  previewImageUrl.value = imgs[idx]
  scale.value = 1
  showImageModal.value = true
  imageLoading.value = true
  try {
    await preloadImage(imgs[idx])
    preloadAdjacent(imgs, idx)
  } catch (err) {
    console.warn('图片加载失败', err)
  } finally {
    imageLoading.value = false
  }
}

// 上一张（循环）
const prevImage = () => {
  if (imageLoading.value) return
  const len = imageList.value.length
  if (len === 0) return
  const newIndex = (currentImageIndex.value - 1 + len) % len
  switchToImage(newIndex)
}

// 下一张（循环）
const nextImage = () => {
  if (imageLoading.value) return
  const len = imageList.value.length
  if (len === 0) return
  const newIndex = (currentImageIndex.value + 1) % len
  switchToImage(newIndex)
}

// 放大/缩小
const zoomIn = () => { scale.value = Math.min(scale.value + 0.2, 3) }
const zoomOut = () => { scale.value = Math.max(scale.value - 0.2, 0.6) }

// 关闭弹窗
const closeModal = () => {
  showImageModal.value = false
  previewImageUrl.value = ''
  scale.value = 1
  imageLoading.value = false
}

// 评论
const commentForm = ref({ content: '' })

const getDefaultComments = (articleId) => {
  if (articleId === '1') {
    return [
      {
        id: 1,
        nickname: '小叶同学',
        avatar: 'https://picsum.photos/100/100?1',
        content: '欢迎来到我的博客！大家可以在这里随意留言～',
        time: '2025-01-01 12:00',
        isAdmin: true,
        like: 7,
        replies: []
      }
    ]
  }
  return []
}

const likeComment = (commentId) => {
  const list = currentCommentList.value
  const comment = list.find(c => c.id === commentId)
  if (comment) comment.like++
  ElMessage.success('点赞成功！')
}
const likeReply = (commentId, replyId) => {
  const list = currentCommentList.value
  const comment = list.find(c => c.id === commentId)
  if (!comment) return
  const reply = comment.replies.find(r => r.id === replyId)
  if (reply) reply.like++
  ElMessage.success('点赞成功！')
}

const commentsStore = ref({})
const currentArticleId = computed(() => route.params.id)
const currentCommentList = computed(() => {
  const id = currentArticleId.value
  if (!commentsStore.value[id]) {
    commentsStore.value[id] = getDefaultComments(id)
  }
  return commentsStore.value[id]
})

const publishComment = (commentId, replyId, content) => {
  if (commentId === undefined && replyId === undefined && content === undefined) {
    if (!commentForm.value.content.trim()) {
      ElMessage.warning('评论内容不能为空~')
      return
    }
    const newComment = {
      id: Date.now(),
      nickname: '游客' + Math.random().toString().slice(-6),
      avatar: `https://picsum.photos/100/100?${Math.random()}`,
      content: commentForm.value.content,
      time: new Date().toLocaleString(),
      isAdmin: false,
      like: 0,
      replies: []
    }
    commentsStore.value[currentArticleId.value].unshift(newComment)
    ElMessage.success('评论发布成功！')
    commentForm.value.content = ''
    return
  }

  const parentComment = currentCommentList.value.find(c => c.id === commentId)
  if (!parentComment) return

  let replyToName = parentComment.nickname
  if (replyId) {
    const targetReply = parentComment.replies.find(r => r.id === replyId)
    if (targetReply) replyToName = targetReply.nickname
  }

  parentComment.replies.unshift({
    id: Date.now(),
    nickname: '游客' + Math.random().toString().slice(-6),
    avatar: `https://picsum.photos/100/100?${Math.random()}`,
    content: content,
    replyTo: replyToName,
    time: new Date().toLocaleString(),
    like: 0
  })
  ElMessage.success('回复成功！')
}

// 表情
const showEmoji = ref(false)
const closeEmojiOutside = (e) => {
  const emojiBox = document.querySelector('.emoji-panel')
  const emojiBtn = document.querySelector('.emoji-btn')
  if (emojiBox && !emojiBox.contains(e.target) && !emojiBtn.contains(e.target)) {
    showEmoji.value = false
  }
}
const insertEmoji = (code) => {
  commentForm.value.content += code
  closeEmoji()
}
const closeEmoji = () => {
  showEmoji.value = false
}

// 路由切换刷新
watch(() => route.params.id, () => {
  fetchDailyDetail()
  commentForm.value.content = ''
})

onMounted(() => {
  fetchDailyDetail()
  // 浏览数 +1（失败静默，不影响页面）
  addDailyViewApi(route.params.id).catch(() => {})
  document.addEventListener('click', closeEmojiOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', closeEmojiOutside)
})
</script>

<template>
  <div class="common-article-detail">
    <div class="article-detail">
      <button class="button-return" @click="$router.go(-1)">
        <el-icon>
          <DArrowLeft />
        </el-icon>返回
      </button>
      <div class="divider"></div>

      <div class="daily-item" v-if="dailyItem">
        <div class="card-header">
          <el-avatar :src="dailyItem.avatar" size="50" />
          <div class="info">
            <div class="username">{{ dailyItem.username }}</div>
            <div class="time">{{ dailyItem.createTime }}</div>
          </div>
        </div>

        <div class="card-content">{{ dailyItem.content }}</div>

        <div class="card-images" v-if="dailyItem.images.length">
          <img v-for="(img, idx) in dailyItem.images" :key="img" :src="img"
            @click.stop="handleImageClick(dailyItem, idx)" draggable="false" />
        </div>

        <div class="card-actions">
          <button class="like-btn" :class="{ active: dailyItem.isLiked }" @click.stop="toggleLike(dailyItem)">
            <font-awesome-icon icon="fa-solid fa-thumbs-up" /> {{ dailyItem.like }}
          </button>
          <button class="comment-btn">
            <el-icon>
              <ChatDotRound />
            </el-icon> {{ dailyItem.comment }}
          </button>
        </div>
      </div>

      <div v-else class="empty-data">暂无日常数据</div>

      <div class="comment-section">
        <div class="comment-publish-box">
          <h3>留下你的想法~</h3>
          <div class="publish-form">
            <div class="comment-textarea-container">
              <textarea v-model="commentForm.content" placeholder="请输入评论内容..." maxlength="500"
                class="comment-textarea"></textarea>
              <div class="emoji-btn" @click.stop="showEmoji = !showEmoji">
                <font-awesome-icon :icon="['fa', 'face-smile']" />
              </div>
              <Emoji v-if="showEmoji" :show="showEmoji" @select="insertEmoji" @close="closeEmoji" />
              <el-button type="primary" size="small" @click="publishComment()" class="publish-btn">
                发表评论
              </el-button>
            </div>
          </div>
        </div>

        <h3>评论</h3>
        <CommentList :comment-list="currentCommentList" @like-comment="likeComment" @like-reply="likeReply"
          @send-reply="publishComment" />
      </div>
    </div>

    <div v-if="showImageModal" class="image-modal" @click.self="closeModal">
      <div class="img-close" @click="closeModal">✕</div>
      <div class="img-prev" @click="prevImage">‹</div>
      <div v-if="imageLoading" class="image-loading-overlay">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <img :src="previewImageUrl" class="preview-image" :style="{ transform: `scale(${scale})` }" draggable="false">
      <div class="img-next" @click="nextImage">›</div>
      <div class="img-zoom">
        <div @click="zoomOut"><el-icon>
            <ZoomOut />
          </el-icon></div>
        <div @click="zoomIn"><el-icon>
            <ZoomIn />
          </el-icon></div>
        <!-- 图片计数器（循环时提示当前位置） -->
        <div class="image-counter" v-if="imageList.length">
          {{ currentImageIndex + 1 }} / {{ imageList.length }}
        </div>
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
  color: var(--text-secondary-color);
  font-size: 16px;
  padding: 60px 0;
}

.comment-section {
  margin-top: 20px;
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
  content: "";
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

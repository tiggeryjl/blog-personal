<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { DArrowLeft, DArrowRight, ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import CommentList from '@/components/Comment.vue'
import Emoji from '@/components/Emoji.vue'

const router = useRouter()
const route = useRoute()

const prevArticle = ref(null)
const nextArticle = ref(null)

// 评论表单
const commentForm = ref({
  content: ''
})

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
// 点赞主评论
const likeComment = (commentId) => {
  const list = currentCommentList.value
  const comment = list.find(c => c.id === commentId)
  if (comment) comment.like++
  ElMessage.success('点赞成功！')
}

// 点赞回复
const likeReply = (commentId, replyId) => {
  const list = currentCommentList.value
  const comment = list.find(c => c.id === commentId)
  if (!comment) return
  const reply = comment.replies.find(r => r.id === replyId)
  if (reply) reply.like++
  ElMessage.success('点赞成功！')
}

// 存储所有文章的评论，key: 文章id, value: 评论数组
const commentsStore = ref({})

// 当前文章ID
const currentArticleId = computed(() => route.params.id)

// 当前文章的评论列表（计算属性，自动响应）
const currentCommentList = computed(() => {
  const id = currentArticleId.value
  if (!commentsStore.value[id]) {
    commentsStore.value[id] = getDefaultComments(id)
  }
  return commentsStore.value[id]
})

const publishComment = (commentId, replyId, content) => {
  // 发布新评论（无参数）
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
    // 注意：要修改当前文章的评论数组（直接操作 commentsStore 中对应的数组）
    commentsStore.value[currentArticleId.value].unshift(newComment)
    ElMessage.success('评论发布成功！')
    commentForm.value.content = ''
    return
  }

  // 回复评论
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

// 跳转方法
const goToArticle = (id) => {
  router.push(`/article/${id}`)
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

// 点击图片打开预览
const handleImageClick = async (e) => {
  if (e.target.tagName !== 'IMG') return
  const imgs = document.querySelectorAll('.article-body img')
  imageList.value = Array.from(imgs).map(img => img.src)
  const clickedIndex = Array.from(imgs).findIndex(img => img === e.target)
  if (clickedIndex === -1) return

  currentImageIndex.value = clickedIndex
  previewImageUrl.value = imageList.value[clickedIndex]
  scale.value = 1
  showImageModal.value = true
  imageLoading.value = true
  try {
    await preloadImage(imageList.value[clickedIndex])
    preloadAdjacent(imageList.value, clickedIndex)
  } catch (err) {
    console.warn('初始图片加载失败', err)
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

const article = ref({
  title: '',
  author: '',
  createTime: '',
  category: '',
  view: 0,
  like: 0,
  // 核心：文章内容（带 <p> <img> 标签）
  content: ''
})
//点赞文章
const like = () => {
  const articleId = route.params.id
  const storageKey = `liked_article_${articleId}`
  if (localStorage.getItem(storageKey)) {
    ElMessage.warning('你已经点过赞了')
    return
  }
  article.value.like++
  localStorage.setItem(storageKey, '1')
  ElMessage.success('点赞成功！')
}

const loadArticle = () => {
  const currentId = route.params.id
  article.value = {
    title: '我的第一篇文章',
    author: '张三',
    createTime: '2025-12-20 12:00',
    category: '技术',
    view: 100,
    like: 10,
    content: `
      <p>大家好，这是文章第一段。大家好，这是文章第一段。大家好，这是文章第一段。大家好，这是文章第一段。大家好，这是文章第一段。大家好，这是文章第一段。</p>
      <p>这是文章第二段，用来展示段落。这是文章第二段，用来展示段落。这是文章第二段，用来展示段落。这是文章第二段，用来展示段落。这是文章第二段，用来展示段落。这是文章第二段，用来展示段落。这是文章第二段，用来展示段落。这是文章第二段，用来展示段落。</p>
      <img src="https://picsum.photos/800/450" />
      <p>这是图片下面的文字。这是图片下面的文字。这是图片下面的文字。这是图片下面的文字。这是图片下面的文字。这是图片下面的文字。这是图片下面的文字。这是图片下面的文字。这是图片下面的文字。这是图片下面的文字。</p>
      <h3>这是小标题</h3>
      <p>这是小标题下面的内容。这是小标题下面的内容。这是小标题下面的内容。这是小标题下面的内容。这是小标题下面的内容。这是小标题下面的内容。这是小标题下面的内容。这是小标题下面的内容。这是小标题下面的内容。这是小标题下面的内容。</p>
    `
  }
  const articleList = [
    { id: '1', title: '第一篇文章' },
    { id: '2', title: '第二篇文章' },
    { id: '3', title: '第三篇文章' },
    { id: '4', title: '第四篇文章' }
  ]
  const currentIndex = articleList.findIndex(item => item.id === currentId)
  prevArticle.value = currentIndex > 0 ? articleList[currentIndex - 1] : null
  nextArticle.value = currentIndex < articleList.length - 1 ? articleList[currentIndex + 1] : null
}

// 表情相关
const showEmoji = ref(false)
const closeEmojiOutside = (e) => {
  const emojiBox = document.querySelector('.emoji-panel')
  const emojiBtn = document.querySelector('.emoji-btn')

  if (emojiBox && !emojiBox.contains(e.target) && !emojiBtn.contains(e.target)) {
    showEmoji.value = false
    // hoverEmoji.value = null
  }
}

const insertEmoji = (code) => {
  if (showEmoji.value) {
    commentForm.value.content += code
    closeEmoji()
  }
}
const closeEmoji = () => {
  showEmoji.value = false
}

watch(() => route.params.id, () => {
  loadArticle()
  commentForm.value.content = ''  // 清空评论输入框
})

// 页面加载时监听点击
onMounted(() => {
  loadArticle()
  document.addEventListener('click', closeEmojiOutside)
})

// 页面离开时清除监听
onUnmounted(() => {
  document.removeEventListener('click', closeEmojiOutside)
})
</script>
<template>
  <div class="common-article-detail">
    <div class="article-detail">
      <button class="button-return" @click="$router.go(-1)"><el-icon>
          <DArrowLeft />
        </el-icon>返回</button>
      <div class="divider"></div>

      <!-- 文章头部 -->
      <div class="article-header">
        <h1 class="title">{{ article.title }}--{{ $route.params.id }}</h1>
        <div class="meta">
          <span>作者：{{ article.author }}</span>
          <span>{{ article.createTime }}</span>
          <span>分类：{{ article.category }}</span>
          <span>浏览 {{ article.view }}</span>
        </div>
      </div>

      <!-- 文章正文 -->
      <div class="content">
        <div class="article-body" v-html="article.content" @click="handleImageClick"></div>
      </div>

      <div class="article-actions">
        <el-button type="primary" @click="like">
          <font-awesome-icon icon="fa-solid fa-thumbs-up" />
          点赞 {{ article.like }}
        </el-button>
      </div>

      <div class="article-pagination">
        <button v-if="prevArticle" class="prev-btn" @click="goToArticle(prevArticle.id)">
          <el-icon>
            <DArrowLeft />
          </el-icon>
          上一章：{{ prevArticle.title }}
        </button>

        <button v-if="nextArticle" class="next-btn" @click="goToArticle(nextArticle.id)">
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

            <!-- 核心：用 div 模拟输入框容器 -->
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
      <!-- 关闭按钮 -->
      <div class="img-close" @click="closeModal">✕</div>

      <!-- 上一张 -->
      <div class="img-prev" @click="prevImage">‹</div>

      <div v-if="imageLoading" class="image-loading-overlay">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <!-- 图片 -->
      <img :src="previewImageUrl" alt="预览" class="preview-image" :style="{ transform: `scale(${scale})` }" @click.stop
        draggable="false" user-select="none" />

      <!-- 下一张 -->
      <div class="img-next" @click="nextImage">›</div>

      <!-- 缩放按钮 -->
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
  content: "";
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
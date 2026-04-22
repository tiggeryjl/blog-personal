<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import CommentList from '@/components/Comment.vue'
import Emoji from '@/components/Emoji.vue'

// 发布留言
const commentForm = ref({
  nickname: '',
  email: '',
  category: '0',
  content: ''
})

// 类型选项
const categoryOptions = [
  { label: '评论留言', value: '0' },
  { label: '反馈建议', value: '1' },
  { label: '申请友链', value: '2' }
]

const commentList = ref([
  {
    id: 1, // 唯一 ID
    nickname: '小叶同学', // 昵称
    avatar: 'https://picsum.photos/100/100?1', // 头像
    content: '欢迎来到我的博客！大家可以在这里随意留言～', // 评论内容
    time: '2025-01-01 12:00', // 评论时间
    isAdmin: true, // 是否为博主
    category: '0', // 留言类型
    like: 7, // 点赞数
    replies: [] // 回复文本集合
  },
  {
    id: 2,
    nickname: '访客',
    avatar: 'https://picsum.photos/100/100?2',
    content: '博客很棒，加油！',
    time: '2025-01-01 13:00',
    isAdmin: false,
    category: '1',
    like: 2,
    replies: []
  }
])
// 统一处理回复（主评论回复 或 回复的回复）
const publishComment = (commentId, replyId, content) => {
  // 发布新评论
  if (commentId === undefined && replyId === undefined && content === undefined) {
    if (!commentForm.value.nickname || !commentForm.value.content.trim()) {
      ElMessage.warning('昵称和留言内容不能为空~')
      return
    }
    commentList.value.unshift({
      id: Date.now(),
      nickname: commentForm.value.nickname,
      avatar: `https://picsum.photos/100/100?${Math.random()}`,
      content: commentForm.value.content,
      category: commentForm.value.category,
      time: new Date().toLocaleString(),
      isAdmin: false,
      like: 0,
      replies: []
    })
    ElMessage.success('留言发布成功！')
    commentForm.value = { nickname: '', email: '', category: '0', content: '' }
    return
  }

  // 回复评论
  const parentComment = commentList.value.find(c => c.id === commentId)
  if (!parentComment) return

  let replyToName = parentComment.nickname
  if (replyId) {
    const targetReply = parentComment.replies.find(r => r.id === replyId)
    if (targetReply) replyToName = targetReply.nickname
  }

  parentComment.replies.unshift({
    id: Date.now(),
    nickname: '我',
    avatar: `https://picsum.photos/100/100?${Math.random()}`,
    content: content,
    replyTo: replyToName,
    time: new Date().toLocaleString(),
    like: 0
  })
  ElMessage.success('回复成功！')
}
const likeComment = (commentId) => {
  const comment = commentList.value.find(c => c.id === commentId)
  if (comment) comment.like++
  ElMessage.success('点赞成功！')
}

// 点赞回复
const likeReply = (commentId, replyId) => {
  const comment = commentList.value.find(c => c.id === commentId)
  if (!comment) return
  const reply = comment.replies.find(r => r.id === replyId)
  if (reply) reply.like++
  ElMessage.success('点赞成功！')
}
// 表情相关（用于留言板）
const showPublishEmoji = ref(false)

const insertPublishEmoji = (code) => {
  commentForm.value.content += code
  showPublishEmoji.value = false
}

// 点击外部关闭表情面板（可选，复用已有的 closeEmojiOutside 逻辑）
const closeEmojiOutside = (e) => {
  const emojiBox = document.querySelector('.publish-emoji-panel')
  const emojiBtn = document.querySelector('.publish-emoji-btn')
  if (emojiBox && !emojiBox.contains(e.target) && !emojiBtn.contains(e.target)) {
    showPublishEmoji.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeEmojiOutside)
})
onUnmounted(() => {
  document.removeEventListener('click', closeEmojiOutside)
})
</script>

<template>
  <div class="common-feedback">
    <div class="card">
      <h3 class="title-center">请大家畅所欲言~~~</h3>
      <div class="publish-layout">
        <img class="left-avatar" src="https://picsum.photos/100/100" alt="头像" />
        <div class="right-form">
          <div class="one-row-form">
            <div class="form-item">
              <label>昵称:</label>
              <input v-model="commentForm.nickname" placeholder="请输入昵称" />
            </div>
            <div class="form-item">
              <label>邮箱:</label>
              <input v-model="commentForm.email" placeholder="邮箱（选填）" />
            </div>
            <div class="form-item">
              <label>类型:</label>
              <select v-model="commentForm.category">
                <option v-for="item in categoryOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </option>
              </select>
            </div>
          </div>
          <div class="textarea-wrapper">
            <textarea v-model="commentForm.content" placeholder="想说点什么..."></textarea>
            <div class="publish-emoji-btn" @click.stop="showPublishEmoji = !showPublishEmoji">
              <font-awesome-icon :icon="['fa', 'face-smile']" />
            </div>
            <Emoji :show="showPublishEmoji" @select="insertPublishEmoji" @close="showPublishEmoji = false"
              class="publish-emoji-panel" />
          </div>
          <div class="btn-center">
            <button class="submit-btn" @click="publishComment()">留下你的想法</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>评论</h3>
      <CommentList :comment-list="commentList" @like-comment="likeComment" @like-reply="likeReply"
        @send-reply="publishComment" />
    </div>
  </div>
</template>

<style scoped>
.common-feedback {
  min-height: calc(100vh - 65px);
  padding: 20px 9%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 24px;
}

.title-center {
  font-size: 30px !important;
  text-align: center !important;
  padding-left: 0 !important;
  margin-bottom: 20px !important;
}

.title-center::before {
  display: none;
}

.publish-layout {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.left-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.right-form {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.one-row-form {
  display: flex;
  gap: 12px;
  align-items: center;
}

.form-item {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.form-item label {
  color: var(--text-color);
  font-size: 14px;
  white-space: nowrap;
}

.form-item input,
.form-item select {
  flex: 1;
  height: 36px;
  padding: 0 10px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--card-secound-bg);
  color: var(--text-main-color);
  outline: none;
  box-sizing: border-box;
}

textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--card-secound-bg);
  color: var(--text-main-color);
  outline: none;
  resize: vertical;
  box-sizing: border-box;
}

/* 留言板表情按钮容器 */
.textarea-wrapper {
  position: relative;
  width: 100%;
}

.textarea-wrapper textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px 40px 12px 12px;
  /* 右侧留出按钮空间 */
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--card-secound-bg);
  color: var(--text-main-color);
  outline: none;
  resize: vertical;
  box-sizing: border-box;
}

/* 表情按钮 - 定位在左下角 */
.publish-emoji-btn {
  position: absolute;
  left: 12px;
  bottom: 12px;
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary-color);
  z-index: 2;
  transition: transform 0.2s;
}

.publish-emoji-btn:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

/* 表情面板 - 出现在按钮上方 */
.publish-emoji-panel {
  position: absolute;
  bottom: calc(100% + 6px);
  left: 0;
  z-index: 100;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  width: 240px;
  max-height: 160px;
  overflow-y: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.btn-center {
  display: flex;
  justify-content: center;
}

.submit-btn {
  padding: 0 25px;
  height: 36px;
  background: var(--primary-color);
  color: #fff;
  border: none;
  border-radius: 18px;
  cursor: pointer;
}

.submit-btn:hover {
  background-color: #ebb1a2b8;
  color: var(--hover-color);
  border: 1px solid #ebb1a2b8;
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

@media (max-width: 768px) {
  .one-row-form {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-item {
    width: 100%;
  }
}
</style>
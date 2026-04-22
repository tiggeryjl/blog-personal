<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

// 发布留言
const commentForm = ref({
  nickname: '',
  email: '',
  category: '',
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
    replying: false, // 是否为回复评论
    replyText: '', // 回复文本
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
    replying: false,
    replyText: '',
    replies: []
  }
])

// 类型映射
const categoryMap = {
  '0': '评论留言',
  '1': '反馈建议',
  '2': '申请友链'
}

// 发布留言
const submitComment = () => {
  if (!commentForm.value.nickname || !commentForm.value.content) {
    ElMessage.warning('昵称和留言内容不能为空~')
    return
  }
  commentList.value.unshift({
    id: Date.now(), // 使用时间戳作为唯一 ID
    nickname: commentForm.value.nickname,
    avatar: `https://picsum.photos/100/100?${Math.random()}`,
    content: commentForm.value.content,
    time: new Date().toLocaleString(),
    isAdmin: false,
    category: commentForm.value.category,
    like: 0,
    replying: false,
    replyText: '',
    replies: []
  })
  ElMessage.success('留言发布成功！')
  commentForm.value = { nickname: '', email: '', category: '', content: '' }
}
// 发送主评论的回复
const sendReply = (commentId) => {
  // const item = commentList.value[index]
  const item = commentList.value.find(c => c.id === commentId)
  if (!item) return

  if (!item.replyText.trim()) return ElMessage.warning('请输入回复内容')

  item.replies.push({
    id: Date.now(), // 🔥 新增：唯一 ID
    nickname: '我',
    avatar: 'https://picsum.photos/100/100?me',
    content: item.replyText,
    replyTo: item.nickname,  // 记录回复的是谁
    time: new Date().toLocaleString(),
    like: 0,
    replying: false,
    replyText: ''
  })
  item.replying = false
  item.replyText = ''
  ElMessage.success('回复成功！')
}
// 发送回复的回复（关键：全部加到同一级）
const sendReplyToReply = (commentId, replyId) => {
  // const mainComment = commentList.value[commentIndex]
  // const targetReply = mainComment.replies[replyIndex]
  const mainComment = commentList.value.find(c => c.id === commentId)
  if (!mainComment) return
  const targetReply = mainComment.replies.find(r => r.id === replyId)
  if (!targetReply) return

  if (!targetReply.replyText.trim()) return ElMessage.warning('请输入回复内容')

  // 加到【同一层 replies】，不嵌套！
  mainComment.replies.push({
    id: Date.now(), // 🔥 新增：唯一 ID
    nickname: '我',
    avatar: 'https://picsum.photos/100/100?me',
    content: targetReply.replyText,
    replyTo: targetReply.nickname, // 记录回复的是谁
    time: new Date().toLocaleString(),
    like: 0,
    replying: false,
    replyText: ''
  })

  targetReply.replying = false
  targetReply.replyText = ''
  ElMessage.success('回复成功！')
}

// ==================== 🔥 通用发布/回复函数（三合一） ====================
const publishComment = (parentId = null, replyId = null) => {
  // 2. 构建新评论对象
  const newItem = {
    id: Date.now(),
    nickname: commentForm.value.nickname,
    avatar: `https://picsum.photos/100/100?${Math.random()}`,
    content: '',
    category: '',
    replyTo: '',
    time: new Date().toLocaleString(),
    isAdmin: false,
    like: 0,
    replying: false,
    replyText: '',
    replies: []
  }

  // ============= 情况 A：回复顶层评论 =============
  if (parentId && !replyId) {
    const parent = commentList.value.find(c => c.id === parentId)
    if (!parent || !parent.replyText.trim()) {
      ElMessage.warning('请输入回复内容')
      return
    }
    newItem.nickname = '我'
    newItem.content = parent.replyText
    newItem.replyTo = parent.nickname
    parent.replies.push(newItem)
    parent.replying = false
    parent.replyText = ''
  }

  // ============= 情况 B：回复别人的回复 =============
  else if (parentId && replyId) {
    const parent = commentList.value.find(c => c.id === parentId)
    const target = parent?.replies.find(r => r.id === replyId)
    if (!parent || !target || !target.replyText.trim()) {
      ElMessage.warning('请输入回复内容')
      return
    }
    newItem.nickname = '我'
    newItem.content = target.replyText
    newItem.replyTo = target.nickname
    parent.replies.push(newItem)
    target.replying = false
    target.replyText = ''
  }

  // ============= 情况 C：发布全新评论 =============
  else {
    if (!commentForm.value.nickname.trim() || !commentForm.value.content.trim()) {
      ElMessage.warning('昵称和留言内容不能为空~')
      return
    }
    newItem.content = commentForm.value.content
    newItem.category = commentForm.value.category
    commentList.value.unshift(newItem)
  }

  ElMessage.success('发布成功！')
  // 清空表单
  commentForm.value = { nickname: '', email: '', category: '', content: '' }
}

// 点赞主评论
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

// 关闭所有回复框
const closeAllReply = () => {
  commentList.value.forEach(c => {
    c.replying = false
    c.replyText = ''
    c.replies.forEach(r => {
      r.replying = false
      r.replyText = ''
    })
  })
}

// 打开/关闭 主评论回复框
const toggleReply = (commentId) => {
  closeAllReply()
  const comment = commentList.value.find(c => c.id === commentId)
  if (comment) {
    comment.replying = !comment.replying
    comment.replyText = ''
  }
}

// 打开/关闭 回复的回复框
const toggleReplyReply = (commentId, replyId) => {
  closeAllReply()
  const comment = commentList.value.find(c => c.id === commentId)
  if (!comment) return
  const reply = comment.replies.find(r => r.id === replyId)
  if (reply) {
    reply.replying = !reply.replying
    reply.replyText = ''
  }
}

// 控制回复显示更多/收起
const showMoreReplies = (commentId) => {
  const comment = commentList.value.find(c => c.id === commentId)
  if (comment) {
    comment.showAllReplies = !comment.showAllReplies
  }
}
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
                <option value="">请选择</option>
                <option v-for="item in categoryOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </option>
              </select>
            </div>
          </div>
          <textarea v-model="commentForm.content" placeholder="想说点什么..."></textarea>
          <div class="btn-center">
            <button class="submit-btn" @click="publishComment()">留下你的想法</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>评论</h3>
      <div class="comment-list">
        <div class="comment-item" v-for="item in commentList" :key="item.id">
          <img class="avatar" :src="item.avatar" alt="头像" />
          <div class="info">
            <div class="top">
              <span class="name">{{ item.nickname }}</span>
              <span v-if="item.isAdmin" class="admin">博主</span>
              <span class="category-tag" v-if="item.category">
                {{ categoryMap[item.category] || '未分类' }}
              </span>
              <span class="time">{{ item.time }}</span>
            </div>

            <div class="content">{{ item.content }}</div>

            <div class="action-row">
              <button class="action-btn" @click="likeComment(item.id)">
                👍 点赞 {{ item.like }}
              </button>
              <button class="action-btn" :class="{ active: item.replying }" @click="toggleReply(item.id)">
                💬 回复
              </button>
            </div>

            <!-- 主评论回复框 -->
            <div class="reply-input-row" v-show="item.replying">
              <input v-model="item.replyText" :placeholder="`回复 @${item.nickname}`" class="reply-input" />
              <button class="send-reply-btn" @click="publishComment(item.id)">发送</button>
              <button class="cancel-reply-btn" @click="closeAllReply">取消</button>
            </div>

            <!-- 所有回复都在同一级展示 + 折叠更多 -->
            <div class="reply-list" v-if="item.replies.length">
              <!-- 默认显示前 3 条 -->
              <div class="reply-item" v-for="r in item.replies.slice(0, 2)" :key="r.id">
                <img class="reply-avatar" :src="r.avatar" />
                <div class="reply-info">
                  <div class="reply-top">
                    <span class="reply-name">
                      {{ r.nickname }}
                      <span v-if="r.replyTo" class="reply-to-text">
                        回复 @{{ r.replyTo }}
                      </span>
                    </span>
                    <span class="reply-time">{{ r.time }}</span>
                  </div>
                  <div class="reply-content">{{ r.content }}</div>
                  <div class="reply-action-row">
                    <button class="action-btn" @click="likeReply(item.id, r.id)">
                      👍 点赞 {{ r.like }}
                    </button>
                    <button class="action-btn" :class="{ active: r.replying }" @click="toggleReplyReply(item.id, r.id)">
                      💬 回复
                    </button>
                  </div>
                  <div class="reply-input-row" v-show="r.replying">
                    <input v-model="r.replyText" :placeholder="`回复 @${r.nickname}`" class="reply-input" />
                    <button class="send-reply-btn" @click="publishComment(item.id, r.id)">发送</button>
                    <button class="cancel-reply-btn" @click="closeAllReply">取消</button>
                  </div>
                </div>
              </div>

              <!-- 超过 3 条显示更多 -->
              <div v-if="item.replies.length > 2">
                <button class="more-btn" v-if="!item.showAllReplies" @click="showMoreReplies(item.id)">
                  查看更多 ({{ item.replies.length - 2 }}) 条回复
                </button>

                <div v-else>
                  <div class="reply-item" v-for="(r, i) in item.replies.slice(2)" :key="i">
                    <img class="reply-avatar" :src="r.avatar" />
                    <div class="reply-info">
                      <div class="reply-top">
                        <span class="reply-name">
                          {{ r.nickname }}
                          <span v-if="r.replyTo" class="reply-to-text">
                            回复 @{{ r.replyTo }}
                          </span>
                        </span>
                        <span class="reply-time">{{ r.time }}</span>
                      </div>
                      <div class="reply-content">{{ r.content }}</div>
                      <div class="reply-action-row">
                        <button class="action-btn" @click="likeReply(item.id, r.id)">
                          👍 点赞 {{ r.like }}
                        </button>
                        <button class="action-btn" :class="{ active: r.replying }"
                          @click="toggleReplyReply(item.id, r.id)">
                          💬 回复
                        </button>
                      </div>
                      <div class="reply-input-row" v-show="r.replying">
                        <input v-model="r.replyText" :placeholder="`回复 @${r.nickname}`" class="reply-input" />
                        <button class="send-reply-btn" @click="publishComment(item.id, r.id)">发送</button>
                        <button class="cancel-reply-btn" @click="closeAllReply">取消</button>
                      </div>
                    </div>
                  </div>
                  <button class="more-btn" @click="showMoreReplies(item.id)">收起</button>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
      <div v-if="commentList.length === 0" class="empty-data">
        暂无留言~~~
      </div>
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

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px dashed var(--border-color);
}

.comment-item:last-child {
  border-bottom: none;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.info {
  flex: 1;
}

.top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.name {
  font-weight: 500;
  color: var(--text-color);
}

.admin {
  background: var(--primary-color);
  color: #fff;
  font-size: 12px;
  padding: 1px 6px;
  border-radius: 6px;
}

.category-tag {
  background: #f0f0f0;
  color: #666;
  font-size: 12px;
  padding: 1px 6px;
  border-radius: 6px;
}

.time {
  font-size: 12px;
  color: var(--text-secondary-color);
  margin-left: auto;
}

.content {
  color: var(--text-main-color);
  line-height: 1.5;
  margin-bottom: 8px;
  word-break: break-all;
}

.action-row {
  display: flex;
  gap: 15px;
}

.action-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary-color);
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn:hover {
  color: var(--primary-color);
}

.action-btn.active {
  color: var(--primary-color) !important;
  font-weight: bold;
}

.reply-input-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.reply-input {
  flex: 1;
  height: 36px;
  padding: 0 10px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  background: var(--card-secound-bg);
  color: var(--text-main-color);
}

.send-reply-btn {
  padding: 0 12px;
  background: var(--primary-color);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}

.cancel-reply-btn {
  padding: 0 12px;
  background: transparent;
  color: var(--text-secondary-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}

/**展开收起按钮样式 */
.more-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 13px;
  cursor: pointer;
  padding: 4px 0;
  margin-top: 4px;
}

.more-btn:hover {
  text-decoration: underline;
}

/* 回复列表（只有一层缩进，不再嵌套）*/
.reply-list {
  margin: 12px 0 0 0;
  padding-left: 16px;
  border-left: 2px solid var(--primary-color);
}

.reply-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
  background-color: var(--card-bg-hover);
  /* 增加圆角 */
  border-radius: 10px;
  /* 增加内边距，让内容不贴边 */
  padding: 12px;
  /* 可选：加一点阴影，让它更有层次感 */
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}

.reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.reply-info {
  flex: 1;
}

.reply-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.reply-name {
  font-size: 14px;
  color: var(--text-color);
  font-weight: 500;
}

/* 回复@xxx 样式 */
.reply-to-text {
  color: var(--primary-color);
  font-weight: normal;
  margin-left: 4px;
  font-size: 13px;
}

.reply-time {
  font-size: 12px;
  color: var(--text-secondary-color);
  margin-left: auto;
}

.reply-content {
  color: var(--text-main-color);
  line-height: 1.5;
  margin-bottom: 6px;
  word-break: break-all;
}

.reply-action-row {
  display: flex;
  gap: 15px;
}

.card {
  opacity: 0;
  transform: translateY(-10px);
  animation: fadeIn 0.5s ease forwards;
}

.card:nth-child(1) {
  animation-delay: 0.1s;
}

.card:nth-child(2) {
  animation-delay: 0.2s;
}

@keyframes fadeIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty-data {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary-color);
  font-size: 16px;
  padding: 30px 0;
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
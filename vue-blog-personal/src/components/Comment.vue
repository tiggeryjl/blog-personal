<script setup>
import { ref, onMounted, onUnmounted, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import Emoji from '@/components/Emoji.vue'

// 只接收评论数据
const props = defineProps({
  commentList: {
    type: Array,
    required: true
  }
})

// 所有事件抛给父页面
const emit = defineEmits([
  'like-comment',
  'like-reply',
  'send-reply'
])

// 类型映射
const categoryMap = {
  '0': '评论留言',
  '1': '反馈建议',
  '2': '申请友链'
}

const commentUI = reactive(new Map())
const replyUI = reactive(new Map())

const getCommentUI = (commentId) => {
  if (!commentUI.has(commentId)) {
    commentUI.set(commentId, {
      replying: false,
      replyText: '',
      replyPageSize: 2,
      expanded: false,     // 评论内容是否展开
      // 是否还有更多
      // hasMore: true
    })
  }
  return commentUI.get(commentId)
}
const loadMoreReplies = (commentId) => {
  const ui = getCommentUI(commentId)
  // 每次增加10条（可根据需要调整）
  ui.replyPageSize += 10
  // 如果已经达到总条数，也可以设置一个标志，但按钮显示条件已经处理
}
const getReplyUI = (commentId, replyId) => {
  const key = `${commentId}-${replyId}`
  if (!replyUI.has(key)) {
    replyUI.set(key, {
      replying: false,
      replyText: '',
      expanded: false,     // 评论内容是否展开
    })
  }
  return replyUI.get(key)
}

// 关闭所有回复框
const closeAllReplies = () => {
  for (let ui of commentUI.values()) {
    ui.replying = false
    ui.replyText = ''
  }
  for (let ui of replyUI.values()) {
    ui.replying = false
    ui.replyText = ''
  }
}

// 切换主评论回复框
const toggleCommentReply = (commentId) => {
  closeAllReplies()
  const ui = getCommentUI(commentId)
  ui.replying = !ui.replying
}

// 切换回复的回复框
const toggleReplyReply = (commentId, replyId) => {
  closeAllReplies()
  const ui = getReplyUI(commentId, replyId)
  ui.replying = !ui.replying
}

// 切换评论内容展开/收起
const toggleCommentExpand = (commentId) => {
  const ui = getCommentUI(commentId)
  ui.expanded = !ui.expanded
}

// 切换回复内容展开/收起
const toggleReplyExpand = (commentId, replyId) => {
  const ui = getReplyUI(commentId, replyId)
  ui.expanded = !ui.expanded
}

const truncateText = (text, maxLen = 100) => {
  if (!text) return ''
  if (text.length <= maxLen) return text
  return text.slice(0, maxLen) + '...'
}

// 发送回复
const sendReply = (commentId, replyId = null) => {
  if (replyId === null) {
    const ui = getCommentUI(commentId)
    const text = ui.replyText.trim()
    if (!text) {
      ElMessage.warning('请输入回复内容')
      return
    }
    emit('send-reply', commentId, null, text)
    ui.replyText = ''
    ui.replying = false
  } else {
    const ui = getReplyUI(commentId, replyId);
    const text = ui.replyText.trim()
    if (!text) {
      ElMessage.warning('请输入回复内容')
      return
    }
    emit('send-reply', commentId, replyId, text)
    ui.replyText = ''
    ui.replying = false
  }
  closeAllReplies()
}

// 表情相关
const activeEmojiReply = ref(null)

const openEmoji = (target) => {
  if (activeEmojiReply.value === target) {
    closeEmoji()
  } else {
    activeEmojiReply.value = target
  }
}
const closeEmoji = () => {
  activeEmojiReply.value = null
}
const insertEmoji = (code) => {
  if (activeEmojiReply.value) {
    activeEmojiReply.value.replyText += code
    closeEmoji()
  }
}
const closeEmojiOutside = (e) => {
  const btn = e.target.closest('.emoji-btn')
  const panel = e.target.closest('.emoji-panel')
  if (!btn && !panel) closeEmoji()
}
onMounted(() => {
  document.addEventListener('click', closeEmojiOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', closeEmojiOutside)
})

watch(() => props.commentList, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    commentUI.clear()
    replyUI.clear()
  }
})
</script>

<template>
  <div class="comment-list-widget">
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

          <div class="content">
            <span v-if="!getCommentUI(item.id).expanded">
              {{ truncateText(item.content, 100) }}
            </span>
            <span v-else>
              {{ item.content }}
            </span>
            <button v-if="item.content.length > 100" class="expand-btn" @click="toggleCommentExpand(item.id)">
              {{ getCommentUI(item.id).expanded ? '收起' : '展开' }}
            </button>
          </div>

          <div class="action-row">
            <button class="action-btn" @click="emit('like-comment', item.id)">
              <font-awesome-icon icon="fa-solid fa-thumbs-up" /> 点赞 {{ item.like }}
            </button>
            <button class="action-btn" :class="{ active: getCommentUI(item.id).replying }"
              @click="toggleCommentReply(item.id)">
              💬 回复
            </button>
          </div>

          <!-- 主评论回复框 -->
          <div class="reply-input-row" v-show="getCommentUI(item.id).replying">
            <div class="reply-input-wrapper">
              <input v-model="getCommentUI(item.id).replyText" :placeholder="`回复 @${item.nickname}`"
                class="reply-input" />

              <div class="emoji-btn" @click.stop="openEmoji(getCommentUI(item.id))">
                <font-awesome-icon :icon="['fa', 'face-smile']" />
              </div>
              <Emoji :show="activeEmojiReply === getCommentUI(item.id)" @select="insertEmoji" @close="closeEmoji"
                class="emoji-panel" />
            </div>
            <button class="send-reply-btn" @click="sendReply(item.id)">发送</button>
            <button class="cancel-reply-btn" @click="closeAllReplies">取消</button>
          </div>

          <!-- 回复列表 -->
          <div class="reply-list" v-if="item.replies.length">
            <div class="reply-item" v-for="r in item.replies.slice(0, getCommentUI(item.id).replyPageSize)" :key="r.id">
              <img class="reply-avatar" :src="r.avatar" />
              <div class="reply-info">
                <div class="reply-top">
                  <span class="reply-name">
                    {{ r.nickname }}
                    <span v-if="r.replyTo" class="reply-to-text">回复 @{{ r.replyTo }}</span>
                  </span>
                  <span class="reply-time">{{ r.time }}</span>
                </div>

                <div class="reply-content">
                  <span v-if="!getReplyUI(item.id, r.id).expanded">
                    {{ truncateText(r.content, 80) }}
                  </span>
                  <span v-else>
                    {{ r.content }}
                  </span>
                  <button v-if="r.content.length > 80" class="expand-btn" @click="toggleReplyExpand(item.id, r.id)">
                    {{ getReplyUI(item.id, r.id).expanded ? '收起' : '展开' }}
                  </button>
                </div>

                <div class="reply-action-row">
                  <button class="action-btn" @click="emit('like-reply', item.id, r.id)">
                    <font-awesome-icon icon="fa-solid fa-thumbs-up" /> 点赞 {{ r.like }}
                  </button>
                  <button class="action-btn" :class="{ active: getReplyUI(item.id, r.id).replying }"
                    @click="toggleReplyReply(item.id, r.id)">
                    💬 回复
                  </button>
                </div>

                <div class="reply-input-row" v-show="getReplyUI(item.id, r.id).replying">
                  <div class="reply-input-wrapper">
                    <input v-model="getReplyUI(item.id, r.id).replyText" :placeholder="`回复 @${r.nickname}`"
                      class="reply-input" />

                    <div class="emoji-btn" @click.stop="openEmoji(getReplyUI(item.id, r.id))">
                      <font-awesome-icon :icon="['fa', 'face-smile']" />
                    </div>
                    <Emoji :show="activeEmojiReply === getReplyUI(item.id, r.id)" @select="insertEmoji"
                      @close="closeEmoji" class="emoji-panel" />
                  </div>
                  <button class="send-reply-btn" @click="sendReply(item.id, r.id)">发送</button>
                  <button class="cancel-reply-btn" @click="closeAllReplies">取消</button>
                </div>

              </div>
            </div>

            <!-- 展开/收起 按钮 -->
            <button v-if="getCommentUI(item.id).replyPageSize < item.replies.length" class="more-btn"
              @click="loadMoreReplies(item.id)">
              加载更多 ({{ item.replies.length - getCommentUI(item.id).replyPageSize }})
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="commentList.length === 0" class="empty-data">
      暂无评论~~~
    </div>
  </div>
</template>

<style scoped>
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

.expand-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 12px;
  cursor: pointer;
  padding: 0 4px;
  margin-left: 8px;
}

.expand-btn:hover {
  text-decoration: underline;
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

.reply-input-wrapper {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
}

.reply-input {
  padding-right: 40px !important;
  flex: 1;
  height: 36px;
  padding: 0 10px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  background: var(--card-secound-bg);
  color: var(--text-main-color);
  padding-right: 40px !important;
}

.reply-input-wrapper {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
}

.reply-input {
  flex: 1;
  height: 36px;
  padding: 0 36px 0 10px;
  /* 右侧留出按钮空间 */
  border-radius: 6px;
  border: 1px solid var(--border-color);
  background: var(--card-secound-bg);
  color: var(--text-main-color);
}

/* 表情按钮 - 定位在输入框右侧内部 */
.reply-input-wrapper .emoji-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary-color);
  background: transparent;
  border: none;
  z-index: 2;
}

/* 表情面板 - 出现在按钮的正上方 */
.reply-input-wrapper .emoji-panel {
  position: absolute;
  bottom: calc(100% + 6px);
  right: 0;
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

.emoji-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  cursor: pointer;
  color: var(--text-secondary-color);
}

.emoji-btn:hover {
  color: var(--primary-color);
}

@media (max-width: 768px) {
  .comment-item {
    gap: 10px;
    padding-bottom: 14px;
  }

  .avatar {
    width: 38px;
    height: 38px;
  }

  .reply-avatar {
    width: 28px;
    height: 28px;
  }

  .top {
    gap: 4px;
  }

  .name,
  .reply-name {
    font-size: 14px;
  }

  .content,
  .reply-content {
    font-size: 14px;
  }

  .reply-list {
    padding-left: 10px;
  }

  .reply-item {
    padding: 10px;
    gap: 8px;
  }

  .action-row,
  .reply-action-row {
    gap: 10px;
  }

  .action-btn {
    font-size: 12px;
  }

  .reply-input-row {
    gap: 6px;
  }

  .reply-input {
    font-size: 14px;
  }

  .send-reply-btn,
  .cancel-reply-btn {
    font-size: 12px;
    padding: 0 10px;
  }
}

@media (max-width: 480px) {
  .comment-item {
    gap: 8px;
  }

  .avatar {
    width: 34px;
    height: 34px;
  }

  .reply-avatar {
    width: 26px;
    height: 26px;
  }

  .reply-list {
    padding-left: 8px;
  }

  .reply-item {
    padding: 8px;
  }

  .action-row,
  .reply-action-row {
    gap: 8px;
  }

  .reply-input {
    height: 34px;
  }
}
</style>
<script setup>
import { ref, nextTick, watch } from 'vue'
import { fetchStream } from '@/api/AIChat.js'

// ========== 状态 ==========
const dialogVisible = ref(false)
const userInput = ref('')
const deepThink = ref(false)
const isStreaming = ref(false)
const messages = ref([])
const totalTokens = ref(0)
const messagesContainer = ref(null)
const currentModel = ref('Flash')

// ========== 快捷指令 ==========
const quickCommands = [
  { label: '写文章', icon: '✍️', prompt: '请帮我写一篇关于 [主题] 的技术文章，要求结构清晰、有代码示例' },
  { label: '润色', icon: '✨', prompt: '请润色以下内容，使表达更专业、流畅' },
  { label: '总结', icon: '📝', prompt: '请用 150 字以内总结以下内容的核心观点' },
  { label: '分析', icon: '🔍', prompt: '请分析以下内容的逻辑结构，并给出改进建议' },
]

// ========== 方法 ==========
function toggleDialog() {
  dialogVisible.value = !dialogVisible.value
}

function closeDialog() {
  dialogVisible.value = false
}

function clearConversation() {
  if (messages.value.length === 0) return
  if (confirm('确定要清除所有对话记录吗？')) {
    messages.value = []
    totalTokens.value = 0
  }
}

function formatMessage(content) {
  // 简单 Markdown 处理（可扩展）
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

async function sendQuickCommand(cmd) {
  // 先填入输入框，用户可修改后再发送
  userInput.value = cmd.prompt
  // 聚焦输入框
  await nextTick()
  document.querySelector('.ai-input')?.focus()
  // 选中全部文字方便替换
  document.querySelector('.ai-input')?.select()
}

async function sendMessage() {
  const content = userInput.value.trim()
  if (!content || isStreaming.value) return

  messages.value.push({ role: 'user', content })
  userInput.value = ''
  scrollToBottom()

  const aiIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })

  isStreaming.value = true
  let localFullText = ''

  try {
    await fetchStream(
      content,
      deepThink.value,
      (newChunk) => {

        const lines = newChunk.split('\n')
        const cleanLines = lines.map(line => {
          const trimmed = line.trim()
          if (trimmed.startsWith('data:')) {
            let content = trimmed.substring(5).trim()
            while (content.startsWith('data:')) {
              content = content.substring(5).trim()
            }
            return content
          }
          return trimmed
        })
        const cleanChunk = cleanLines.join('\n')

        if (!cleanChunk) return

        // 每次收到新数据块，直接拼接到 localFullText
        localFullText += cleanChunk
        // 更新消息内容
        messages.value[aiIndex].content = localFullText
        scrollToBottom()
        totalTokens.value = Math.ceil(localFullText.length / 1.5)
      }
    )

    // 🔥 流结束后，localFullText 就是完整内容
    console.log('✅ 完整内容:', localFullText)
    // 可以在这里做最终处理（比如更新数据库）

    // 如果流结束后内容为空，可设置提示
    if (!localFullText) {
      messages.value[aiIndex].content = '（收到空响应）'
    }

  } catch (error) {
    console.error('流式请求失败:', error)
    messages.value[aiIndex].content = '❌ 出错了，请重试'
  } finally {
    isStreaming.value = false
    scrollToBottom()
  }
}

// 监听消息变化，自动滚动
watch(messages, () => scrollToBottom(), { deep: true })
</script>

<template>
  <div class="ai-assistant">
    <!-- ========== 悬浮图标 ========== -->
    <div class="ai-float-btn" @click="toggleDialog" :class="{ active: dialogVisible }">
      <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
        <path
          d="M12 2a10 10 0 0 1 10 10 10 10 0 0 1-10 10 10 10 0 0 1-5.5-1.6L4 22l1.6-5.5A10 10 0 0 1 2 12 10 10 0 0 1 12 2z" />
        <path d="M8 12h.01M12 12h.01M16 12h.01" />
      </svg>
    </div>

    <!-- ========== 弹窗 ========== -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="dialogVisible" class="ai-dialog-overlay" @click.self="closeDialog">
          <div class="ai-dialog">
            <!-- 标题栏 -->
            <div class="dialog-header">
              <div class="header-left">
                <span class="ai-icon">🤖</span>
                <span class="ai-title">AI 助手</span>
                <span class="model-badge">{{ currentModel }}</span>
              </div>
              <div class="header-actions">
                <button class="header-btn" @click="clearConversation" title="清除对话">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
                  </svg>
                </button>
                <button class="header-btn" @click="closeDialog">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M18 6L6 18M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            <!-- 对话区域 -->
            <div class="dialog-body" ref="messagesContainer">
              <div v-if="messages.length === 0" class="empty-state">
                <div class="empty-icon">🤖</div>
                <div class="empty-title">你好！我是你的 AI 写作助手</div>
                <div class="empty-desc">我可以帮你写文章、润色、总结、分析日志等</div>
              </div>
              <div v-else class="messages-list">
                <div v-for="(msg, idx) in messages" :key="idx" class="message-item" :class="msg.role">
                  <div class="message-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
                  <div class="message-content" v-html="formatMessage(msg.content)"></div>
                </div>
                <!-- 流式加载动画 -->
                <div v-if="isStreaming" class="message-item assistant">
                  <div class="message-avatar">🤖</div>
                  <div class="message-content streaming">
                    <span class="cursor-blink">▊</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 快捷指令 -->
            <div class="quick-actions">
              <button v-for="cmd in quickCommands" :key="cmd.label" class="quick-btn" @click="sendQuickCommand(cmd)">
                {{ cmd.icon }} {{ cmd.label }}
              </button>
            </div>

            <!-- 输入区域 -->
            <div class="dialog-footer">
              <div class="input-wrapper">
                <textarea v-model="userInput" class="ai-input" rows="2" placeholder="输入你的指令或文章内容... (Ctrl+Enter 发送)"
                  @keydown.ctrl.enter="sendMessage" :disabled="isStreaming" />
                <div class="input-toolbar">
                  <div class="toolbar-left">
                    <label class="toggle-label">
                      <input type="checkbox" v-model="deepThink" />
                      <span>🧠 深度思考</span>
                    </label>
                    <span class="token-counter">已用 {{ totalTokens }} Token</span>
                  </div>
                  <button class="send-btn" @click="sendMessage" :disabled="!userInput.trim() || isStreaming">
                    发送 →
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
/* ========== 悬浮图标 ========== */
.ai-float-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.45);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.ai-float-btn:hover {
  transform: scale(1.08) rotate(-4deg);
  box-shadow: 0 8px 32px rgba(99, 102, 241, 0.55);
}

.ai-float-btn.active {
  transform: scale(0.9);
}

.ai-float-btn svg {
  width: 28px;
  height: 28px;
}

/* ========== 弹窗遮罩 ========== */
.ai-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ========== 弹窗主体 ========== */
.ai-dialog {
  width: 720px;
  max-width: 92vw;
  height: 580px;
  max-height: 85vh;
  background: #1e1e2f;
  border-radius: 20px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

/* ========== 标题栏 ========== */
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-icon {
  font-size: 22px;
}

.ai-title {
  font-size: 16px;
  font-weight: 600;
  color: #e4e4e7;
}

.model-badge {
  font-size: 11px;
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  padding: 2px 10px;
  border-radius: 12px;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.header-btn {
  background: transparent;
  border: none;
  color: #a1a1aa;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.header-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #e4e4e7;
}

/* ========== 对话区域 ========== */
.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #181825;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  max-width: 88%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.user .message-content {
  background: #6366f1;
  color: #fff;
  border-radius: 16px 4px 16px 16px;
}

.message-item.assistant .message-content {
  background: #27273a;
  color: #d4d4d8;
  border-radius: 4px 16px 16px 16px;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.05);
}

.message-content {
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  min-height: 20px;
}

.message-content code {
  background: rgba(0, 0, 0, 0.3);
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.message-content strong {
  color: #e4e4e7;
}

.cursor-blink {
  animation: blink 1s infinite;
  color: #818cf8;
}

@keyframes blink {

  0%,
  50% {
    opacity: 1;
  }

  51%,
  100% {
    opacity: 0;
  }
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #71717a;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #d4d4d8;
  margin-bottom: 6px;
}

.empty-desc {
  font-size: 14px;
}

/* ========== 快捷指令 ========== */
.quick-actions {
  display: flex;
  gap: 8px;
  padding: 10px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
  flex-wrap: wrap;
}

.quick-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #a1a1aa;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.3);
  color: #e4e4e7;
}

/* ========== 输入区域 ========== */
.dialog-footer {
  padding: 12px 20px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
  background: #1e1e2f;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-input {
  width: 100%;
  background: #27273a;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 12px 14px;
  color: #e4e4e7;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  min-height: 52px;
  max-height: 150px;
  transition: border-color 0.2s;
  font-family: inherit;
}

.ai-input:focus {
  outline: none;
  border-color: #6366f1;
}

.ai-input:disabled {
  opacity: 0.5;
}

.ai-input::placeholder {
  color: #52525b;
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #a1a1aa;
  cursor: pointer;
}

.toggle-label input[type="checkbox"] {
  accent-color: #6366f1;
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.token-counter {
  font-size: 12px;
  color: #52525b;
}

.send-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  color: #fff;
  padding: 8px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.4);
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ========== 动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ========== 滚动条 ========== */
.dialog-body::-webkit-scrollbar {
  width: 4px;
}

.dialog-body::-webkit-scrollbar-track {
  background: transparent;
}

.dialog-body::-webkit-scrollbar-thumb {
  background: #3f3f5a;
  border-radius: 4px;
}

.dialog-body::-webkit-scrollbar-thumb:hover {
  background: #52527a;
}
</style>
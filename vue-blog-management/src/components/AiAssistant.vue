<script setup>
import { ref, nextTick, watch } from 'vue';
import { fetchStream } from '@/api/AIChat.js';

// ========== 状态 ==========
const dialogVisible = ref(false);
const userInput = ref('');
const deepThink = ref(false);
const isStreaming = ref(false);
const messages = ref([]);
const totalTokens = ref(0);
const messagesContainer = ref(null);
const currentModel = ref('Flash');

// ========== 快捷指令 ==========
const quickCommands = [
  {
    label: '写文章',
    icon: '✍️',
    prompt: '请帮我写一篇关于 [主题] 的技术文章，要求结构清晰、有代码示例',
  },
  { label: '润色', icon: '✨', prompt: '请润色以下内容，使表达更专业、流畅' },
  {
    label: '总结',
    icon: '📝',
    prompt: '请用 150 字以内总结以下内容的核心观点',
  },
  {
    label: '分析',
    icon: '🔍',
    prompt: '请分析以下内容的逻辑结构，并给出改进建议',
  },
];

// ========== 方法 ==========
function toggleDialog() {
  dialogVisible.value = !dialogVisible.value;
}

function closeDialog() {
  dialogVisible.value = false;
}

function clearConversation() {
  if (messages.value.length === 0) return;
  if (confirm('确定要清除所有对话记录吗？')) {
    messages.value = [];
    totalTokens.value = 0;
  }
}

function formatMessage(content) {
  // 简单 Markdown 处理（可扩展）
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.*?)`/g, '<code>$1</code>');
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
}

async function sendQuickCommand(cmd) {
  // 先填入输入框，用户可修改后再发送
  userInput.value = cmd.prompt;
  // 聚焦输入框
  await nextTick();
  document.querySelector('.ai-input')?.focus();
  // 选中全部文字方便替换
  document.querySelector('.ai-input')?.select();
}

async function sendMessage() {
  const content = userInput.value.trim();
  if (!content || isStreaming.value) return;

  messages.value.push({ role: 'user', content });
  userInput.value = '';
  scrollToBottom();

  const aiIndex = messages.value.length;
  messages.value.push({ role: 'assistant', content: '' });

  isStreaming.value = true;
  let localFullText = '';

  try {
    await fetchStream(content, deepThink.value, (newChunk) => {
      const lines = newChunk.split('\n');
      const cleanLines = lines.map((line) => {
        const trimmed = line.trim();
        if (trimmed.startsWith('data:')) {
          let content = trimmed.substring(5).trim();
          while (content.startsWith('data:')) {
            content = content.substring(5).trim();
          }
          return content;
        }
        return trimmed;
      });
      const cleanChunk = cleanLines.join('\n');

      if (!cleanChunk) return;

      // 每次收到新数据块，直接拼接到 localFullText
      localFullText += cleanChunk;
      // 更新消息内容
      messages.value[aiIndex].content = localFullText;
      scrollToBottom();
      totalTokens.value = Math.ceil(localFullText.length / 1.5);
    });

    // 🔥 流结束后，localFullText 就是完整内容
    console.log('✅ 完整内容:', localFullText);
    // 可以在这里做最终处理（比如更新数据库）

    // 如果流结束后内容为空，可设置提示
    if (!localFullText) {
      messages.value[aiIndex].content = '（收到空响应）';
    }
  } catch (error) {
    console.error('流式请求失败:', error);
    messages.value[aiIndex].content = '❌ 出错了，请重试';
  } finally {
    isStreaming.value = false;
    scrollToBottom();
  }
}

// 监听消息变化，自动滚动
watch(messages, () => scrollToBottom(), { deep: true });
</script>

<template>
  <div class="ai-assistant">
    <!-- ========== 悬浮图标 ========== -->
    <div class="ai-float-btn" @click="toggleDialog" :class="{ active: dialogVisible }">
      <div class="light-1"></div>
      <div class="light-2"></div>
      <div class="cloud">
        <div class="wave-col col1"></div>
        <div class="wave-col col2"></div>
        <div class="wave-col col3"></div>
      </div>
    </div>

    <!-- ========== 弹窗 ========== -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="dialogVisible" class="ai-dialog-overlay" @click.self="closeDialog">
          <div class="ai-dialog">
            <!-- 标题栏 -->
            <div class="dialog-header">
              <div class="header-left">
                <div class="cloud-avatar">
                  <div class="mini-cloud">
                    <div class="mini-wave-col mw1"></div>
                    <div class="mini-wave-col mw2"></div>
                    <div class="mini-wave-col mw3"></div>
                  </div>
                </div>
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
                <div class="empty-icon">
                  <div class="mini-cloud">
                    <div class="mini-wave-col mw1"></div>
                    <div class="mini-wave-col mw2"></div>
                    <div class="mini-wave-col mw3"></div>
                  </div>
                </div>
                <div class="empty-title">你好！我是你的 AI 写作助手</div>
                <div class="empty-desc">我可以帮你写文章、润色、总结、分析日志等</div>
              </div>
              <div v-else class="messages-list">
                <div v-for="(msg, idx) in messages" :key="idx" class="message-item" :class="msg.role">
                  <div class="message-avatar">
                    <span v-if="msg.role === 'user'">👤</span>
                    <div v-else class="message-cloud-avatar">
                      <div class="mini-cloud">
                        <div class="mini-wave-col mw1"></div>
                        <div class="mini-wave-col mw2"></div>
                        <div class="mini-wave-col mw3"></div>
                      </div>
                    </div>
                  </div>
                  <div class="message-content" v-html="formatMessage(msg.content)"></div>
                </div>
                <!-- 流式加载动画 -->
                <div v-if="isStreaming" class="message-item assistant">
                  <div class="message-cloud-avatar">
                    <div class="mini-cloud">
                      <div class="mini-wave-col mw1"></div>
                      <div class="mini-wave-col mw2"></div>
                      <div class="mini-wave-col mw3"></div>
                    </div>
                  </div>
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
                <textarea
                  v-model="userInput"
                  class="ai-input"
                  rows="2"
                  placeholder="输入你的指令或文章内容... (Ctrl+Enter 发送)"
                  @keydown.ctrl.enter="sendMessage"
                  :disabled="isStreaming"
                />
                <div class="input-toolbar">
                  <div class="toolbar-left">
                    <button
                      type="button"
                      class="deep-think-btn"
                      :class="{ active: deepThink }"
                      @click="deepThink = !deepThink"
                    >
                      <span class="deep-think-dot"></span>
                      深度思考
                    </button>
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
/* ========== 悬浮云朵气泡按钮【全新样式】 ========== */
.ai-float-btn {
  position: fixed;
  right: 36px;
  bottom: 40px;
  width: 70px;
  height: 55px;
  cursor: pointer;
  transition: 0.2s all ease;
  z-index: 999;
}
.ai-float-btn:hover {
  transform: translateY(-5px);
}
.ai-float-btn:active,
.ai-float-btn.active {
  transform: scale(0.95);
}

.light-1 {
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(100, 190, 255, 0.28) 0%, transparent 70%);
  animation: glow 2.8s infinite ease-in-out;
  filter: blur(6px);
}
.light-2 {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(200, 140, 255, 0.22) 0%, transparent 70%);
  animation: glow 2.8s infinite ease-in-out reverse;
  filter: blur(4px);
}

.cloud {
  width: 100%;
  height: 100%;
  background: #fff;
  border-radius: 50px;
  border-bottom-right-radius: 12px;
  box-shadow: 0 6px 20px rgba(110, 161, 255, 0.555);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  position: relative;
  z-index: 2;
}

.wave-col {
  width: 7px;
  border-radius: 99px;
  background: linear-gradient(#40b9ff, #a25bff);
}
.col1 {
  height: 24px;
  animation: bounce1 1s infinite alternate ease;
}
.col2 {
  height: 16px;
  animation: bounce2 0.8s infinite alternate ease;
}
.col3 {
  height: 20px;
  animation: bounce3 1.2s infinite alternate ease;
}

/* 气泡专属动画，追加到css末尾 */
@keyframes glow {
  0%,
  100% {
    opacity: 0.6;
    transform: scale(0.96);
  }
  50% {
    opacity: 1;
    transform: scale(1.04);
  }
}
@keyframes bounce1 {
  0% {
    height: 15px;
  }
  100% {
    height: 27px;
  }
}
@keyframes bounce2 {
  0% {
    height: 22px;
  }
  100% {
    height: 13px;
  }
}
@keyframes bounce3 {
  0% {
    height: 14px;
  }
  100% {
    height: 25px;
  }
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
.cloud-avatar {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-cloud-avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon .mini-cloud {
  width: 52px;
  height: 42px;
}

.mini-cloud {
  width: 100%;
  height: 100%;
  background: #ffffff;
  border-radius: 50px;
  border-bottom-right-radius: 10px;
  border: 1px solid #e6e9ff;
  box-shadow: 0 4px 12px rgba(110, 161, 255, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  position: relative;
}

.mini-wave-col {
  width: 5px;
  border-radius: 99px;
  background: linear-gradient(#40b9ff, #a25bff);
}

.mw1 {
  height: 18px;
  animation: miniWave1 1s infinite alternate ease;
}

.mw2 {
  height: 13px;
  animation: miniWave2 0.85s infinite alternate ease;
}

.mw3 {
  height: 15px;
  animation: miniWave3 1.15s infinite alternate ease;
}

@keyframes miniWave1 {
  0% {
    height: 13px;
  }
  100% {
    height: 20px;
  }
}

@keyframes miniWave2 {
  0% {
    height: 17px;
  }
  100% {
    height: 11px;
  }
}

@keyframes miniWave3 {
  0% {
    height: 12px;
  }
  100% {
    height: 18px;
  }
}

.ai-dialog {
  width: 720px;
  max-width: 92vw;
  height: 580px;
  max-height: 85vh;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  animation: dialogIn 0.28s cubic-bezier(0.22, 0.61, 0.36, 1);
}

@keyframes dialogIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(12px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* ========== 标题栏 ========== */
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #ebeef5;
  background: linear-gradient(180deg, #fbfcff, #ffffff);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.model-badge {
  font-size: 11px;
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
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
  color: #909399;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.header-btn:hover {
  background: #f5f7fa;
  color: #303133;
}

/* ========== 对话区域 ========== */
.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #e9eaec;
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
  background: linear-gradient(135deg, #eef0ff, #e3e8ff);
  color: #303133;
  border: 1px solid #e0e4ff;
  border-radius: 16px 4px 16px 16px;
}

.message-item.assistant .message-content {
  background: #ffffff;
  color: #303133;
  border: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(31, 35, 41, 0.06);
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
  background: #f0f2f5;
}

.message-content {
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  min-height: 20px;
}

.message-content code {
  background: #f0f2f5;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.message-content strong {
  color: #303133;
}

.cursor-blink {
  animation: blink 1s infinite;
  color: #6366f1;
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
  /* 实现整个空区域在聊天框上下左右全部居中 */
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 14px;
  padding: 20px;
  color: #909399;
}

.empty-icon {
  /* 图标盒子自身居中 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon .mini-cloud {
  width: 52px;
  height: 42px;
}

.empty-title {
  font-size: 19px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.empty-desc {
  font-size: 14px;
  opacity: 0.85;
  margin: 0;
  text-align: center;
}

/* ========== 快捷指令 ========== */
.quick-actions {
  display: flex;
  gap: 8px;
  padding: 10px 20px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.quick-btn {
  background: #f5f7fa;
  border: 1px solid #bec3ce;
  color: #606266;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #eef0ff;
  border-color: #c7cdff;
  color: #303133;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.12);
}

/* ========== 输入区域 ========== */
.dialog-footer {
  padding: 12px 22px 16px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
  background: #ffffff;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-input {
  width: 100%;
  box-sizing: border-box;
  background: #e1e3e6;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 12px;
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  min-height: 52px;
  max-height: 150px;
  transition: border-color 0.2s, box-shadow 0.2s;
  font-family: inherit;
}

.ai-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.ai-input:disabled {
  opacity: 0.5;
}

.ai-input::placeholder {
  color: #a8abb2;
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 6px 0 2px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.deep-think-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  border: 1px solid #bec3ce;
  border-radius: 8px;
  padding: 5px 12px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.deep-think-btn:hover {
  border-color: #c7cdff;
  color: #303133;
}

.deep-think-btn.active {
  background: #6366f1;
  border-color: #6366f1;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.35);
}

.deep-think-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c0c4cc;
  transition: background 0.2s;
}

.deep-think-btn.active .deep-think-dot {
  background: #ffffff;
}

.token-counter {
  font-size: 12px;
  color: #a8abb2;
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
  background: #dcdfe6;
  border-radius: 4px;
}

.dialog-body::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>

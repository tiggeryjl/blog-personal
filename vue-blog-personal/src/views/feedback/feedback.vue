<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { User } from '@element-plus/icons-vue';
import CommentList from '@/components/Comment.vue';
import Emoji from '@/components/Emoji.vue';
import { requireLogin } from '@/utils/auth';
import { useUserStore } from '@/store/userloginstatus';
import { addCommentReplyApi, addMessageCommentApi, getMessageCommentListApi } from '@/api/comment.js';
import { likeApi, LIKE_TARGET_TYPE } from '@/api/like.js';
import { emailExistsApi, loginApi, registerByEmailApi } from '@/api/auth.js';
import { isGuestLiked, rememberGuestLiked } from '@/utils/guestLike.js';

const userStore = useUserStore();
const isLogin = computed(() => !!userStore.user_token);
const userInfo = computed(() => userStore.userInfo || {});

const EMAIL_REGEX = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;

// 留言表单：身份取登录账号，仅类型与内容可填
const commentForm = ref({
  category: '0',
  content: '',
});

// 类型选项
const categoryOptions = [
  { label: '评论留言', value: '0' },
  { label: '反馈建议', value: '1' },
  { label: '申请友链', value: '2' },
];

// ========== 留言列表 ==========
const commentList = ref([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const listLoading = ref(false);
const listLoadFailed = ref(false);
let latestListRequestId = 0;

// 统一补齐字段
const normalizeComment = (item) => ({
  ...item,
  userNickname: item.userNickname || '匿名用户',
  userAvatar: item.userAvatar,
  likeNum: Number(item.likeNum) || 0,
  liked: item.liked === true || (!isLogin.value && isGuestLiked(LIKE_TARGET_TYPE.COMMENT, item.id)),
  admin: item.admin === true,
  replies: normalizeComments(item.replies),
});

const normalizeComments = (list) => (Array.isArray(list) ? list : []).map(normalizeComment);

const getMessageList = async (targetPage = page.value) => {
  const requestId = ++latestListRequestId;
  listLoading.value = true;
  listLoadFailed.value = false;
  try {
    const result = await getMessageCommentListApi({ page: targetPage, pageSize: pageSize.value });
    if (requestId !== latestListRequestId) return false;
    if (result?.code === 200) {
      const data = result.data || {};
      commentList.value = normalizeComments(data.rows);
      total.value = Number(data.total) || 0;
      page.value = targetPage;
      return true;
    }
    listLoadFailed.value = true;
    ElMessage.error(result?.msg || '留言加载失败，请稍后重试');
    return false;
  } catch (error) {
    if (requestId === latestListRequestId) {
      listLoadFailed.value = true;
      ElMessage.error('留言加载失败，请稍后重试');
    }
    return false;
  } finally {
    if (requestId === latestListRequestId) {
      listLoading.value = false;
    }
  }
};

const handlePageChange = (newPage) => {
  getMessageList(newPage);
};

// 登录态变化时刷新列表，保证点赞状态正确
watch(isLogin, () => {
  latestListRequestId += 1;
  getMessageList(1);
});

// ========== 发布留言 / 回复 ==========
const publishing = ref(false);

const publishComment = async (commentId, replyId, content) => {
  if (!requireLogin('登录后才能留言哦~')) return false;

  // 发表新留言
  if (commentId === undefined && replyId === undefined && content === undefined) {
    const submitted = (commentForm.value.content || '').trim();
    if (!submitted) {
      ElMessage.warning('留言内容不能为空~');
      return false;
    }
    if (publishing.value) return false;
    publishing.value = true;
    try {
      const result = await addMessageCommentApi({
        msgType: Number(commentForm.value.category),
        content: submitted,
      });
      if (result?.code === 200) {
        commentForm.value.content = '';
        ElMessage.success('留言发布成功！');
        await getMessageList(1);
        return true;
      }
      ElMessage.error(result?.msg || '留言发布失败，请稍后重试');
      return false;
    } catch (error) {
      ElMessage.error('留言发布失败，请稍后重试');
      return false;
    } finally {
      publishing.value = false;
    }
  }

  // 回复留言
  if (!content || !content.trim()) {
    ElMessage.warning('回复内容不能为空');
    return false;
  }
  const parentId = replyId || commentId;
  if (!parentId) return false;
  try {
    const result = await addCommentReplyApi({ parentId, content: content.trim() });
    if (result?.code === 200) {
      ElMessage.success('回复成功！');
      await getMessageList(page.value);
      return true;
    }
    ElMessage.error(result?.msg || '回复失败，请稍后重试');
    return false;
  } catch (error) {
    ElMessage.error('回复失败，请稍后重试');
    return false;
  }
};

// ========== 点赞 ==========
const likingTargets = new Set();

const handleLike = async (targetId, onSuccess) => {
  const key = `${LIKE_TARGET_TYPE.COMMENT}_${targetId}`;
  if (likingTargets.has(key)) return;
  likingTargets.add(key);
  try {
    const result = await likeApi({ targetType: LIKE_TARGET_TYPE.COMMENT, targetId });
    if (result?.code === 200) {
      const data = result.data || {};
      onSuccess(data);
      // ElMessage.success(data.liked ? '点赞成功！' : '已取消点赞');
    } else {
      ElMessage.error(result?.msg || '操作失败，请稍后重试');
    }
  } catch (error) {
    ElMessage.error('点赞失败，请稍后重试');
  } finally {
    likingTargets.delete(key);
  }
};

const likeComment = (commentId) => {
  const comment = commentList.value.find((item) => item.id === commentId);
  if (!comment) return;
  if (comment.liked) {
    ElMessage.warning('你已经点过赞了');
    return;
  }
  handleLike(commentId, (data) => {
    comment.liked = data.liked !== false;
    if (typeof data.likeCount === 'number') comment.likeNum = data.likeCount;
    if (!isLogin.value) rememberGuestLiked(LIKE_TARGET_TYPE.COMMENT, commentId);
  });
};

const likeReply = (commentId, replyId) => {
  const comment = commentList.value.find((item) => item.id === commentId);
  if (!comment) return;
  const reply = (comment.replies || []).find((item) => item.id === replyId);
  if (!reply) return;
  if (reply.liked) {
    ElMessage.warning('你已经点过赞了');
    return;
  }
  handleLike(replyId, (data) => {
    reply.liked = data.liked !== false;
    if (typeof data.likeCount === 'number') reply.likeNum = data.likeCount;
    if (!isLogin.value) rememberGuestLiked(LIKE_TARGET_TYPE.COMMENT, replyId);
  });
};

// ========== 简约登录 / 注册 ==========
const authEmail = ref('');
const authNickname = ref('');
const authDialogVisible = ref(false);
const authMode = ref('login'); // login=已注册邮箱登录；register=未注册邮箱注册
const authPassword = ref('');
const authConfirmPwd = ref('');
const authSubmitting = ref(false);

// 输入邮箱：已注册走登录，未注册走注册
const handleQuickAuth = async () => {
  const email = authEmail.value.trim();
  if (!EMAIL_REGEX.test(email)) {
    ElMessage.warning('请输入正确的邮箱');
    return;
  }
  try {
    const result = await emailExistsApi(email);
    if (result?.code !== 200) {
      ElMessage.error(result?.msg || '操作失败，请稍后重试');
      return;
    }
    authMode.value = result.data ? 'login' : 'register';
    if (authMode.value === 'register' && !authNickname.value.trim()) {
      ElMessage.warning('该邮箱还未注册，请先填写昵称');
      return;
    }
    authPassword.value = '';
    authConfirmPwd.value = '';
    authDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试');
  }
};

const submitQuickAuth = async () => {
  const email = authEmail.value.trim();
  const password = authPassword.value;
  if (!password) {
    ElMessage.warning('请输入密码');
    return;
  }
  if (authMode.value === 'register') {
    if (!authNickname.value.trim()) {
      ElMessage.warning('请输入昵称');
      return;
    }
    if (password.length < 6 || password.length > 20) {
      ElMessage.warning('密码长度需为6-20位');
      return;
    }
    if (password !== authConfirmPwd.value) {
      ElMessage.warning('两次密码输入不一致');
      return;
    }
  }
  if (authSubmitting.value) return;
  authSubmitting.value = true;
  try {
    if (authMode.value === 'register') {
      const registerResult = await registerByEmailApi({
        email,
        nickname: authNickname.value.trim(),
        password,
        confirmPwd: authConfirmPwd.value,
      });
      if (registerResult?.code !== 200) {
        ElMessage.error(registerResult?.msg || '注册失败，请稍后重试');
        return;
      }
    }

    const loginResult = await loginApi({ loginName: email, password });
    if (loginResult?.code === 200) {
      userStore.loginSuccess(loginResult.data);
      ElMessage.success(authMode.value === 'register' ? '注册并登录成功！' : '登录成功！');
      authDialogVisible.value = false;
      authEmail.value = '';
      authNickname.value = '';
      authPassword.value = '';
      authConfirmPwd.value = '';
      await getMessageList(1);
    } else {
      ElMessage.error(
        loginResult?.msg || (authMode.value === 'register' ? '注册成功，请手动登录' : '登录失败，请检查邮箱或密码')
      );
    }
  } catch (error) {
    ElMessage.error(authMode.value === 'register' ? '注册失败，请稍后重试' : '登录失败，请稍后重试');
  } finally {
    authSubmitting.value = false;
  }
};

// ========== 表情 ==========
const showPublishEmoji = ref(false);

const insertPublishEmoji = (code) => {
  commentForm.value.content += code;
  showPublishEmoji.value = false;
};

const closeEmojiOutside = (e) => {
  const emojiBox = document.querySelector('.publish-emoji-panel');
  const emojiBtn = document.querySelector('.publish-emoji-btn');
  if (emojiBox && !emojiBox.contains(e.target) && !emojiBtn.contains(e.target)) {
    showPublishEmoji.value = false;
  }
};

onMounted(() => {
  getMessageList(1);
  document.addEventListener('click', closeEmojiOutside);
});

onUnmounted(() => {
  latestListRequestId += 1;
  document.removeEventListener('click', closeEmojiOutside);
});
</script>

<template>
  <div class="common-feedback">
    <div class="card">
      <h3 class="title-center">请大家畅所欲言~~~</h3>
      <div class="publish-layout">
        <img v-if="isLogin && userInfo.avatar" class="left-avatar" :src="userInfo.avatar" alt="头像" />
        <div v-else class="left-avatar guest-avatar">
          <el-icon><User /></el-icon>
        </div>
        <div class="right-form">
          <!-- 已登录 -->
          <div v-if="isLogin" class="one-row-form">
            <div class="form-item">
              <label>昵称:</label>
              <input :value="userInfo.nickname" disabled />
            </div>
            <div class="form-item">
              <label>邮箱:</label>
              <input :value="userInfo.email" disabled />
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

          <!-- 未登录 -->
          <div v-else class="quick-auth">
            <div class="one-row-form">
              <div class="form-item">
                <label>昵称:</label>
                <input v-model="authNickname" placeholder="请输入昵称" @keyup.enter="handleQuickAuth" />
              </div>
              <div class="form-item">
                <label>邮箱:</label>
                <input v-model="authEmail" type="email" placeholder="请输入邮箱 *" @keyup.enter="handleQuickAuth" />
              </div>
              <div class="form-item">
                <label>类型:</label>
                <select v-model="commentForm.category">
                  <option v-for="item in categoryOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </option>
                </select>
              </div>
              <button class="quick-auth-btn" @click="handleQuickAuth">确认输入</button>
            </div>
          </div>

          <div class="textarea-wrapper">
            <textarea
              v-model="commentForm.content"
              maxlength="500"
              :placeholder="isLogin ? '想说点什么...' : '登录后即可留言~'"
            ></textarea>
            <div class="publish-emoji-btn" @click.stop="showPublishEmoji = !showPublishEmoji">
              <font-awesome-icon :icon="['fa', 'face-smile']" />
            </div>
            <Emoji
              :show="showPublishEmoji"
              @select="insertPublishEmoji"
              @close="showPublishEmoji = false"
              class="publish-emoji-panel"
            />
          </div>
          <div class="btn-center">
            <button class="submit-btn" :disabled="publishing" @click="publishComment()">
              {{ publishing ? '发布中...' : '留下你的想法' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>
        评论
        <span v-if="total" class="comment-count">({{ total }})</span>
      </h3>

      <div v-if="listLoadFailed" class="list-error">
        <span>留言加载失败</span>
        <button class="link-btn" @click="getMessageList(page)">重新加载</button>
      </div>

      <CommentList
        v-loading="listLoading"
        :comment-list="commentList"
        :on-send-reply="publishComment"
        @like-comment="likeComment"
        @like-reply="likeReply"
      />

      <div v-if="total > pageSize" class="pagination-row">
        <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          background
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 简约登录对话框 -->
    <el-dialog
      v-model="authDialogVisible"
      class="auth-dialog-el"
      width="420px"
      align-center
      :close-on-click-modal="false"
    >
      <template #header>
        <h3 class="auth-dialog-title">{{ authMode === 'register' ? '完成注册' : '登录' }}</h3>
      </template>

      <div class="auth-dialog">
        <p class="auth-dialog-email">{{ authEmail }}</p>
        <p class="auth-dialog-tip">
          {{
            authMode === 'register'
              ? '该邮箱还没有账号，设置密码后即可完成注册并自动登录'
              : '请输入该邮箱对应的密码完成登录'
          }}
        </p>
        <el-input
          v-model="authPassword"
          type="password"
          show-password
          placeholder="请输入密码"
          @keyup.enter="submitQuickAuth"
        />
        <el-input
          v-if="authMode === 'register'"
          v-model="authConfirmPwd"
          type="password"
          show-password
          placeholder="请再次输入密码确认"
          @keyup.enter="submitQuickAuth"
        />
      </div>
      <template #footer>
        <div class="auth-dialog-footer">
          <button type="button" class="auth-cancel-btn" @click="authDialogVisible = false">取消</button>
          <button type="button" class="auth-submit-btn" :disabled="authSubmitting" @click="submitQuickAuth">
            {{ authSubmitting ? '处理中…' : authMode === 'register' ? '注册并登录' : '登录' }}
          </button>
        </div>
      </template>
    </el-dialog>
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

/* 未登录：默认线条用户图标 */
.guest-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--card-secound-bg);
  color: var(--text-secondary-color);
  font-size: 30px;
  box-sizing: border-box;
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

.form-item input:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

/* 未登录时的简约登录/注册 */
.quick-auth {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-auth-btn {
  flex-shrink: 0;
  align-self: center;
  height: 36px;
  padding: 0 18px;
  border: none;
  border-radius: 18px;
  background: var(--primary-color);
  color: #fff;
  cursor: pointer;
}

.quick-auth-btn:hover {
  background-color: #ebb1a2b8;
  color: var(--hover-color);
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

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
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

.comment-count {
  font-size: 14px;
  color: var(--text-secondary-color);
  font-weight: normal;
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

.list-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 56px;
  color: var(--text-secondary-color);
}

.link-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 14px;
}

.link-btn:hover {
  text-decoration: underline;
}

.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 简约登录/注册对话框（外观对齐 RSS 订阅弹窗） */
.auth-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.auth-dialog-title {
  margin: 0;
  color: var(--text-color);
  font-size: 16px;
  font-weight: 600;
  line-height: 1;
}

.auth-dialog-email {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
  word-break: break-all;
}

.auth-dialog-tip {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary-color);
}

/* 让 el-input 的视觉与 RSS 弹窗里的 subscribe-input 保持一致 */
.auth-dialog :deep(.el-input__wrapper) {
  padding: 7px 14px;
  border-radius: 8px;
  background-color: var(--card-secound-bg);
  box-shadow: 0 0 0 1px var(--border-color) inset;
  transition: box-shadow 0.2s ease;
}

.auth-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.auth-dialog :deep(.el-input__inner) {
  height: auto;
  color: var(--text-color);
  font-size: 14px;
}

.auth-dialog :deep(.el-input__inner::placeholder) {
  color: var(--text-prompt-color);
}

.auth-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.auth-cancel-btn {
  padding: 7px 18px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: transparent;
  color: var(--text-secondary-color);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.auth-cancel-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.auth-submit-btn {
  flex-shrink: 0;
  padding: 7px 16px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #409eff, #003ae5);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.auth-submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.35);
}

.auth-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

<!--
  el-dialog 会渲染到组件作用域之外，scoped 的 :deep() 拿不到它的内部节点，
  所以登录/注册弹窗的外观覆写单独放在这个非 scoped 块里，用 .auth-dialog-el 限定作用范围。
-->
<style>
.el-dialog.auth-dialog-el {
  max-width: calc(100vw - 40px);
  padding: 20px 22px 18px;
  border-radius: 12px;
  background-color: var(--card-secound-bg);
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
}

.auth-dialog-el .el-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 0 16px;
  padding: 0;
}

.auth-dialog-el .el-dialog__headerbtn {
  position: static;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  color: var(--text-secondary-color);
  transition: all 0.2s ease;
}

.auth-dialog-el .el-dialog__headerbtn:hover {
  background-color: var(--card-bg-hover);
  color: var(--hover-color);
}

.auth-dialog-el .el-dialog__close {
  color: inherit;
  font-size: 14px;
}

.auth-dialog-el .el-dialog__body {
  padding: 0;
  color: var(--text-color);
}

.auth-dialog-el .el-dialog__footer {
  padding: 0;
  margin-top: 18px;
}
</style>

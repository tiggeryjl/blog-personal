<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { CopyDocument } from '@element-plus/icons-vue';
import { useUserStore } from '@/store/userloginstatus';
import MarqueeText from '@/components/MarqueeText.vue';
import { getPublicLinkListApi, applyLinkApi, getApplicationsApi, urgeLinkApi } from '@/api/link.js';

const userStore = useUserStore();

const linkList = ref([]);
const applications = ref([]);
const urgingId = ref(null);
const urgedIds = ref(new Set());

const isLogin = computed(() => !!userStore.user_token);

// 判断是否本人申请
const isOwn = (item) => {
  const userEmail = (userStore.userInfo?.email || '').trim().toLowerCase();
  const linkEmail = String(item.linkEmail || '')
    .trim()
    .toLowerCase();
  return isLogin.value && !!userEmail && !!linkEmail && userEmail === linkEmail;
};

const copyUrl = async () => {
  const blogUrl = 'https://your-blog-url.com';
  try {
    await navigator.clipboard.writeText(blogUrl);
    ElMessage.success('复制成功！');
  } catch (err) {
    ElMessage.error('复制失败，请重试或者联系博主!');
  }
};

// 申请弹窗
const showApplyForm = ref(false);
const applyForm = ref({
  linkName: '',
  linkUrl: '',
  linkAvatar: '',
  linkDesc: '',
  email: '',
});

// 加载已通过的友联
const loadLinks = async () => {
  try {
    const result = await getPublicLinkListApi();
    if (result.code === 200) {
      linkList.value = result.data || [];
    }
  } catch (err) {
    ElMessage.error('友链加载失败，请稍后重试');
  }
};

// 加载全部待审核/已拒绝的友链申请
const loadApplications = async () => {
  try {
    const result = await getApplicationsApi();
    if (result.code === 200) {
      applications.value = result.data || [];
    }
  } catch (err) {
    ElMessage.error('友链申请加载失败，请稍后重试');
  }
};

// 催促审核（仅登录且本人申请可操作）
const urge = async (item) => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录后再催促审核');
    return;
  }
  if (!isOwn(item)) {
    ElMessage.warning('只能催促本人申请的友链');
    return;
  }
  if (urgingId.value) return;
  urgingId.value = item.id;
  try {
    const result = await urgeLinkApi(item.id);
    if (result.code === 200) {
      urgedIds.value.add(item.id);
      urgedIds.value = new Set(urgedIds.value);
      ElMessage.success('已催促站长审核，请耐心等待');
    } else {
      ElMessage.error(result.msg || '催促失败，请稍后重试');
    }
  } catch (err) {
    ElMessage.error('催促失败，请稍后重试');
  } finally {
    urgingId.value = null;
  }
};

const isUrged = (item) => urgedIds.value.has(item.id);
const formRef = ref(null);
const rules = {
  linkName: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  linkUrl: [
    { required: true, message: '请输入网站链接', trigger: 'blur' },
    { pattern: /^https?:\/\/.+/i, message: '请输入合法的网址（http/https）', trigger: 'blur' },
  ],
};

// 打开申请弹窗：重置表单并清除上一次的校验提示
const openApply = () => {
  applyForm.value = { linkName: '', linkUrl: '', linkAvatar: '', linkDesc: '', email: '' };
  showApplyForm.value = true;
  nextTick(() => formRef.value?.clearValidate());
};

// 弹窗关闭后清除校验状态
const handleDialogClosed = () => {
  formRef.value?.clearValidate();
};

// 提交申请
const submitApply = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch (err) {
    return; // 校验不通过，不提交
  }
  try {
    const result = await applyLinkApi({
      linkName: applyForm.value.linkName.trim(),
      linkUrl: applyForm.value.linkUrl.trim(),
      linkAvatar: applyForm.value.linkAvatar.trim(),
      linkDesc: applyForm.value.linkDesc.trim(),
      email: applyForm.value.email.trim(),
    });
    if (result.code === 200) {
      ElMessage.success('申请成功！请等待博主审核');
      showApplyForm.value = false;
      applyForm.value = { linkName: '', linkUrl: '', linkAvatar: '', linkDesc: '', email: '' };
      loadApplications();
    } else {
      ElMessage.error(result.msg || '申请失败，请稍后重试');
    }
  } catch (err) {
    ElMessage.error('申请失败，请稍后重试');
  }
};

// 新窗口打开
const goLink = (url) => window.open(url, '_blank');

// 时间显示统一格式
const formatTime = (t) => {
  if (!t) return '';
  return String(t).replace('T', ' ').slice(0, 10);
};

onMounted(() => {
  loadLinks();
  loadApplications();
});
</script>

<template>
  <div class="links-container">
    <div class="links">
      <!-- 标题 -->
      <div class="links-header">
        <h2>友链</h2>
        <p>发现更多优质网站｜记录一些优质的个人博客，常来串门～｜定期更新</p>
        <div class="divider"></div>

        <!-- 个人信息 + 交换规则 -->
        <div class="links-info">
          <div class="my-blog-info">
            <h3>我的博客信息</h3>
            <div class="my-card">
              <img src="https://picsum.photos/100/100" alt="我的头像" />
              <div class="my-detail">
                <div class="info-row">
                  <span class="info-label">网站名称:</span>
                  <span class="my-name">小叶同学</span>
                  <button class="copy-button tooltip" @click="copyUrl">
                    <el-icon><CopyDocument /></el-icon>复制头像url
                    <span class="tooltip-text">点击复制我的头像url</span>
                  </button>
                </div>
                <div class="info-row">
                  <span class="info-label">网站简介:</span>
                  <span class="my-desc">你的博客简介｜记录生活与技术</span>
                </div>
                <div class="info-row">
                  <span class="info-label">网站链接:</span>
                  <span class="my-url">https://your-blog-url.com</span>
                </div>
              </div>
            </div>
          </div>

          <div class="link-rules">
            <h3>友联交换规则</h3>
            <ul>
              <li>个人原创博客，内容积极健康</li>
              <li>无恶意广告、跳转、违规内容</li>
              <!-- <li>无法访问的网站会定期清理</li> -->
              <li>可点击下方按钮申请友链</li>
              <li>申请后可通过留言催促博主处理,博主都能看到哦！</li>
            </ul>
          </div>
        </div>

        <div class="apply-area">
          <p class="apply-tip">想和我交换友链吗？请点击按钮申请吧～</p>
          <button class="button" type="primary" @click="openApply">申请友联</button>
        </div>
        <div class="divider"></div>
      </div>

      <!-- 已通过友联 -->
      <div class="block">
        <div class="block-title">
          已通过友联
          <span class="count">{{ linkList.length }}</span>
        </div>
        <div class="links-grid">
          <div class="link-card" v-for="item in linkList" :key="item.id" @click="goLink(item.linkUrl)">
            <img :src="item.linkAvatar" :alt="item.linkName" class="avatar" />
            <h4 class="name">{{ item.linkName }}</h4>
            <p class="desc"><MarqueeText :text="item.linkDesc" /></p>
          </div>
          <div v-if="linkList.length === 0" class="empty-data">还未有申请友链~~~</div>
        </div>
      </div>

      <!-- 全部友链申请 -->
      <div class="block my-apply-block">
        <div class="block-title">
          友链申请列表
          <span class="count">{{ applications.length }}</span>
        </div>
        <p class="block-desc">展示所有待审核与已拒绝的友链申请，按申请时间从新到旧排列</p>

        <div class="links-grid apply-grid">
          <div class="link-card apply-card" v-for="item in applications" :key="item.id">
            <!-- @click="goLink(item.linkUrl)"> -->
            <img :src="item.linkAvatar" :alt="item.linkName" class="avatar" />
            <div class="apply-card-body">
              <div class="apply-card-head">
                <el-tag :type="item.auditStatus === 0 ? 'warning' : 'danger'" size="small">
                  {{ item.auditStatus === 0 ? '待审核' : '已拒绝' }}
                </el-tag>
                <h4 class="name"><MarqueeText :text="item.linkName" /></h4>
              </div>
              <p class="desc"><MarqueeText :text="item.linkDesc || '这位博主没有填写简介'" /></p>
              <div class="apply-card-foot">
                <span class="apply-time">申请时间:&nbsp;&nbsp;{{ formatTime(item.createTime) }}</span>
                <el-button
                  v-if="isOwn(item)"
                  class="urge-btn"
                  type="primary"
                  size="small"
                  :disabled="isUrged(item)"
                  :loading="urgingId === item.id"
                  @click.stop="urge(item)"
                >
                  {{ isUrged(item) ? '已催促' : '催促审核' }}
                </el-button>
              </div>
            </div>
          </div>
          <div v-if="applications.length === 0" class="empty-data">暂无友链申请~~~</div>
        </div>
      </div>
    </div>

    <!-- 申请弹窗 -->
    <el-dialog class="el-dialog" v-model="showApplyForm" title="申请友联" width="500px" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="applyForm" label-width="80px" :rules="rules">
        <el-form-item label="博客名称" prop="linkName">
          <el-input v-model="applyForm.linkName" placeholder="请输入博客名称" />
        </el-form-item>
        <el-form-item label="博客地址" prop="linkUrl">
          <el-input v-model="applyForm.linkUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="头像地址">
          <el-input v-model="applyForm.linkAvatar" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="applyForm.linkDesc" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="applyForm.email" placeholder="用于接收结果，提交后可凭邮箱查看申请进度" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyForm = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ========== 动画 ========== */
.links-header,
.links-info,
.apply-area,
.button,
.copy-button,
.block {
  opacity: 0;
  transform: translateY(-15px);
  transition: all 0.5s ease;
  animation: fadeInUp 0.6s ease forwards;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 整体容器 ========== */
.links-container {
  min-height: calc(100vh - 65px);
  padding: 20px 9%;
}

.links {
  width: 100%;
  padding: 24px;
  background-color: var(--card-bg);
  border-radius: 16px;
  backdrop-filter: blur(6px);
}

/* ========== 标题区域 ========== */
.links-header {
  text-align: center;
  margin-bottom: 2rem;
  animation-delay: 0.1s;
}

.links-header h2 {
  font-size: 30px;
  font-weight: 600;
  color: var(--text-color);
  letter-spacing: 2px;
}

.links-header p {
  font-size: 17px;
  color: var(--text-main-color);
  margin: 8px 0 0;
}

.divider {
  height: 1px;
  background: var(--border-color);
  margin: 14px 0 22px;
  opacity: 0.35;
}

/* ========== 个人信息 + 交换规则 ========== */
.links-info {
  display: flex;
  flex-wrap: wrap;
  gap: 1.6rem;
  margin-bottom: 1.6rem;
  animation-delay: 0.2s;
}

.link-rules,
.my-blog-info {
  flex: 1;
  min-width: 280px;
  background: var(--card-secound-bg);
  padding: 1.3rem 1.8rem;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  text-align: left;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.link-rules:hover,
.my-blog-info:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.link-rules h3,
.my-blog-info h3 {
  font-size: 1.1rem;
  position: relative;
  color: var(--text-color);
  margin-bottom: 1rem;
  padding-left: 14px;
  text-align: left;
}

/* 标题竖线 */
.link-rules h3::before,
.my-blog-info h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 4px;
  height: 20px;
  background: var(--primary-color);
  border-radius: 2px;
}

.link-rules ul {
  list-style: none;
  padding-left: 0.4rem;
  line-height: 1.9;
  color: var(--text-main-color);
  font-size: 0.92rem;
}

.link-rules li::before {
  content: '✦';
  margin-right: 8px;
  color: var(--primary-color);
  font-size: 0.8rem;
}

/* ========== 我的博客信息卡片 ========== */
.my-card {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.my-card img {
  width: 62px;
  height: 62px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--primary-color);
  padding: 2px;
}

.my-detail {
  line-height: 1.5;
  padding-left: 6px;
  min-width: 0;
}

.info-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
  line-height: 1.8;
}

.info-label {
  font-weight: 600;
  color: var(--text-color);
  margin-right: 8px;
  white-space: nowrap;
}

.my-name {
  font-weight: bold;
  font-size: 1rem;
  margin-right: 8px;
  color: var(--text-color);
}

.my-desc,
.my-url {
  font-size: 0.95rem;
  color: var(--text-secondary-color);
  margin: 0;
  word-break: break-all;
}

.copy-button {
  margin-left: 12px;
  padding: 0 12px;
  height: 26px;
  background-color: var(--primary-color);
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  border-radius: 13px;
  border: 1px solid var(--primary-color);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.25s ease;
}

.copy-button:hover {
  background-color: var(--hover-color);
  color: #fff;
  border-color: var(--hover-color);
}

/* 提示框样式 */
.tooltip {
  position: relative;
  display: inline-block;
}

.tooltip .tooltip-text {
  visibility: hidden;
  background-color: var(--card-bg);
  color: var(--text-secondary-color);
  text-align: center;
  border-radius: 6px;
  padding: 6px 12px;
  position: absolute;
  z-index: 1;
  bottom: 135%;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 12px;
  font-weight: normal;
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
  backdrop-filter: blur(4px);
  border: 1px solid var(--border-color);
}

.tooltip .tooltip-text::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  margin-left: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: rgba(0, 0, 0, 0.85) transparent transparent transparent;
}

.tooltip:hover .tooltip-text {
  visibility: visible;
  opacity: 1;
}

/* ========== 申请按钮 ========== */
.apply-area {
  animation-delay: 0.3s;
}

.apply-tip {
  font-size: 0.9rem;
  color: var(--text-secondary-color);
  margin: 0 0 12px;
}

.button {
  width: 120px;
  height: 38px;
  background-color: var(--primary-color);
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  border-radius: 19px;
  border: 1px solid var(--primary-color);
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(0, 126, 251, 0.25);
}

.button:hover {
  background-color: var(--hover-color);
  color: #fff;
  border-color: var(--hover-color);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(251, 71, 0, 0.3);
}

/* ========== 区块标题 ========== */
.block {
  animation-delay: 0.45s;
}

.my-apply-block {
  animation-delay: 0.6s;
  margin-top: 2rem;
}

.block-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 14px;
  padding-left: 14px;
  position: relative;
}

.block-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 5px;
  width: 4px;
  height: 22px;
  background: var(--primary-color);
  border-radius: 2px;
}

.block-title .count {
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: 12px;
  background: var(--primary-color);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.block-desc {
  font-size: 0.9rem;
  color: var(--text-secondary-color);
  margin: -6px 0 14px;
}

/* ========== 已通过友联 ========== */
.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1.4rem;
}

.link-card {
  background: var(--card-secound-bg);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 1.5rem 1rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.link-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  border-color: var(--primary-color);
  background: var(--card-bg-hover);
}

.avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--primary-color);
  padding: 2px;
}

.name {
  font-weight: 600;
  margin: 0.6rem 0 0.3rem;
  color: var(--text-color);
}

.desc {
  font-size: 0.85rem;
  color: var(--text-main-color);
  margin: 0;
  overflow: hidden;
}

/* ========== 友链申请卡片（左右布局） ========== */
.apply-grid {
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
}

.apply-card {
  display: flex;
  align-items: center;
  gap: 14px;
  text-align: left;
  padding: 1rem 1.1rem;
}

.apply-card .avatar {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
}

.apply-card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.apply-card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.apply-card-head .name {
  flex: 1;
  min-width: 0;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.apply-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.apply-time {
  font-size: 0.75rem;
  color: var(--text-secondary-color);
}

.urge-btn {
  margin: 0;
  flex-shrink: 0;
}

.empty-data {
  padding: 26px 0;
  text-align: center;
  color: var(--text-secondary-color);
  font-size: 15px;
}

/* ========== 响应式样式 ========== */
@media (max-width: 1200px) {
  .links-container {
    padding: 20px 5%;
  }

  .links {
    padding: 20px 16px;
  }
}

@media (max-width: 992px) {
  .links-container {
    padding: 15px 4%;
  }

  .links-header h2 {
    font-size: 26px;
  }

  .links-header p {
    font-size: 16px;
  }

  .link-rules,
  .my-blog-info {
    padding: 1.1rem 1.4rem;
  }

  .links-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }

  .apply-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  }
}

@media (max-width: 768px) {
  .links-container {
    padding: 12px 3%;
  }

  .links {
    padding: 16px 12px;
    border-radius: 12px;
  }

  .links-header h2 {
    font-size: 24px;
  }

  .links-header p {
    font-size: 14px;
  }

  .links-info {
    gap: 1rem;
  }

  .link-rules,
  .my-blog-info {
    min-width: 100%;
  }

  .my-card {
    flex-direction: row;
    align-items: center;
  }

  .copy-button {
    font-size: 13px;
    padding: 0 8px;
    height: 24px;
    margin-left: 8px;
  }

  .button {
    width: 110px;
    height: 34px;
    font-size: 15px;
  }

  .links-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 0.9rem;
  }

  .apply-grid {
    grid-template-columns: 1fr;
  }

  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto !important;
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
  }
}

@media (max-width: 576px) {
  .links-container {
    padding: 10px 2%;
  }

  .links {
    padding: 14px 10px;
  }

  .links-header h2 {
    font-size: 22px;
  }

  .links-header p {
    font-size: 13px;
  }

  .divider {
    margin: 10px 0 16px;
  }

  .my-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .my-card img {
    width: 56px;
    height: 56px;
  }

  .info-row {
    align-items: flex-start;
  }

  .copy-button {
    margin-left: 0;
    margin-top: 4px;
  }

  .links-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 0.7rem;
  }

  .apply-grid {
    grid-template-columns: 1fr;
  }

  .link-card {
    padding: 1rem 0.6rem;
  }

  .avatar {
    width: 54px;
    height: 54px;
  }
}

@media (max-width: 400px) {
  .links-grid {
    grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  }

  .avatar {
    width: 46px;
    height: 46px;
  }

  .name {
    font-size: 0.9rem;
  }

  .desc {
    font-size: 0.78rem;
  }
}
</style>

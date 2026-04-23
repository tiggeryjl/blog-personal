<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const linkList = [
  {
    name: "博主A",
    desc: "专注前端开发，分享Vue实战",
    avatar: "https://picsum.photos/100/100?random=1",
    url: "https://example1.com",
  },
  {
    name: "博主B",
    desc: "生活随笔｜摄影｜日常记录",
    avatar: "https://picsum.photos/100/100?random=2",
    url: "https://example2.com",
  },
  {
    name: "博主C",
    desc: "后端技术分享，Java/Golang",
    avatar: "https://picsum.photos/100/100?random=3",
    url: "https://example3.com",
  },
  {
    name: "博主D",
    desc: "设计爱好者，UI/UX分享",
    avatar: "https://picsum.photos/100/100?random=4",
    url: "https://example4.com",
  },
  {
    name: "博主D",
    desc: "设计爱好者，UI/UX分享",
    avatar: "https://picsum.photos/100/100?random=4",
    url: "https://example4.com",
  },
  {
    name: "博主D",
    desc: "设计爱好者，UI/UX分享",
    avatar: "https://picsum.photos/100/100?random=4",
    url: "https://example4.com",
  },
  {
    name: "博主D",
    desc: "设计爱好者，UI/UX分享",
    avatar: "https://picsum.photos/100/100?random=4",
    url: "https://example4.com",
  },
  {
    name: "博主D",
    desc: "设计爱好者，UI/UX分享",
    avatar: "https://picsum.photos/100/100?random=4",
    url: "https://example4.com",
  },
];

const copyUrl = async () => {
  const blogUrl = 'https://your-blog-url.com'
  try {
    await navigator.clipboard.writeText(blogUrl)
    ElMessage.success('复制成功！')
  } catch (err) {
    ElMessage.error('复制失败，请重试或者联系博主!')
  }
}

// 申请弹窗
const showApplyForm = ref(false)
const applyForm = ref({
  name: '',
  url: '',
  avatar: '',
  desc: '',
  email: ''
})

// 加载已通过的友联
const loadLinks = async () => {
}

// 提交申请
const submitApply = async () => {
  await axios.post('/api/links/apply', applyForm.value)
  ElMessage.success('申请成功！请等待站长审核')
  showApplyForm.value = false
}

// 新窗口打开
const goLink = (url) => window.open(url, '_blank')

onMounted(() => loadLinks())
</script>

<template>
  <div class="links-container">
    <div class="links">
      <!-- 标题 -->
      <div class="links-header">
        <h2>友链</h2>
        <p>发现更多优质网站｜记录一些优质的个人博客，常来串门～｜定期更新</p>
        <div class="divider"></div>
        <div class="links-info">

          <div class="my-blog-info">
            <h3>我的博客信息</h3>
            <div class="my-card">
              <img src="https://picsum.photos/100/100" alt="我的头像" />
              <div class="my-detail">
                <div class="info-row">
                  <span class="info-label">网站名称:</span>
                  <span class="my-name">小叶同学</span>
                  <button class="copy-button tooltip" @click="copyUrl">点击复制url
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

        <button class="button" type="primary" @click="showApplyForm = true" style="margin-top: 10px">
          申请友联
        </button>
        <div class="divider"></div>
      </div>

      <!-- 友联展示区（只显示已通过） -->
      <div class="links-grid">
        <div class="link-card" v-for="item in linkList" :key="item.id" @click="goLink(item.url)">
          <img :src="item.avatar" :alt="item.name" class="avatar" />
          <h4 class="name">{{ item.name }}</h4>
          <p class="desc">{{ item.desc }}</p>
        </div>
        <div v-if="linkList.length === 0" class="empty-data">
          还未有申请友链~~~
        </div>
      </div>
    </div>

    <!-- 申请弹窗 -->
    <el-dialog class="el-dialog" v-model="showApplyForm" title="申请友联" width="500px">
      <el-form v-model="applyForm" label-width="80px">
        <el-form-item label="博客名称">
          <el-input v-model="applyForm.name" placeholder="请输入博客名称" />
        </el-form-item>
        <el-form-item label="博客地址">
          <el-input v-model="applyForm.url" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="头像地址">
          <el-input v-model="applyForm.avatar" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="applyForm.desc" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="applyForm.email" placeholder="用于接收结果" />
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
.links-header,
.links-info,
.button,
.copy-button,
.links-grid {
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

.links-container {
  min-height: calc(100vh - 65px);
  padding: 20px 9%;
}

.links {
  width: 100%;
  padding: 20px;
  background-color: var(--card-bg);
  border-radius: 16px;
}

/* 标题区域 */
.links-header {
  text-align: center;
  margin-bottom: 2.5rem;
  animation-delay: 0.1s;
}

.links-header h2 {
  font-size: 30px;
  font-weight: 500;
  color: var(--text-color);
}

.divider {
  height: 2px;
  background: var(--border-color);
  margin: 11px 0px;
}

.links-header p {
  font-size: 18px;
  color: var(--text-main-color);
}

/* 规则 + 个人信息区域 */
.links-info {
  display: flex;
  flex-wrap: wrap;
  gap: 2rem;
  margin-bottom: 3rem;
  animation-delay: 0.25s;
}

.link-rules,
.my-blog-info {
  flex: 1;
  min-width: 280px;
  background: var(--card-secound-bg);
  padding: 1.2rem 3rem;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  text-align: left;
}

.link-rules h3,
.my-blog-info h3 {
  font-size: 1.1rem;
  position: relative;
  color: var(--text-color);
  margin-bottom: 1rem;
  padding-left: 14px;
  /* 竖线和文字间距 */
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
  padding-left: 1.2rem;
  line-height: 1.8;
  color: var(--text-main-color);
  font-size: 0.9rem;
}

/* 个人信息卡片 */
.my-card {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.my-card img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.my-detail {
  line-height: 1.5;
  padding-left: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
  line-height: 1.8;
}

.info-label {
  font-weight: 500;
  color: var(--text-color);
  margin-right: 8px;
  white-space: nowrap;
}

.my-name {
  font-weight: bold;
  font-size: 1rem;
  margin-right: 8px;
}

.my-desc,
.my-url {
  font-size: 0.95rem;
  color: #666;
  margin: 0;
}

.copy-button {
  margin-left: 15px;
  padding: 0 10px;
  height: 25px;
  background-color: var(--primary-color);
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
  border-radius: 10px;
  border: 1px solid var(--primary-color);
  cursor: pointer;
}

.copy-button:hover {
  background-color: #ebb1a2b8;
  color: var(--hover-color);
  border: 1px solid #ebb1a2b8;
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
  content: "";
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

.button {
  width: 100px;
  height: 36px;
  background-color: var(--primary-color);
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
  border-radius: 18px;
  border: 1px solid var(--primary-color);
  animation-delay: 0.4s;
}

.button:hover {
  background-color: #ebb1a2b8;
  color: var(--hover-color);
  border: 1px solid #ebb1a2b8;
}

.divider {
  height: 1px;
  background: var(--border-color);
  margin-bottom: 20px;
}

.links-grid {
  animation-delay: 0.55s;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1.5rem;
}

.link-card {
  background: var(--card-secound-bg);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 1.5rem 1rem;
  text-align: center;
  cursor: pointer;
  transition: 0.3s ease;
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
}

.name {
  font-weight: 600;
  margin: 0.5rem 0;
  color: var(--text-color);
}

.desc {
  font-size: 0.85rem;
  color: var(--text-main-color);
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

/* ========== 响应式样式 ========== */
/* 中等屏幕（平板横屏、小笔记本） */
@media (max-width: 1200px) {
  .links-container {
    padding: 20px 5%;
  }

  .links {
    padding: 20px 15px;
  }
}

/* 平板设备 */
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
    padding: 1rem 1.5rem;
  }

  .my-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .my-card img {
    width: 70px;
    height: 70px;
    margin-bottom: 8px;
  }

  .my-detail {
    padding-left: 0;
  }

  .info-row {
    flex-wrap: wrap;
  }

  .copy-button {
    margin-left: 0;
    margin-top: 5px;
  }

  .links-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }
}

/* 手机横屏、小平板 */
@media (max-width: 768px) {
  .links-container {
    padding: 12px 3%;
  }

  .links {
    padding: 15px 12px;
    border-radius: 12px;
  }

  .links-header {
    margin-bottom: 1.5rem;
  }

  .links-header h2 {
    font-size: 24px;
  }

  .links-header p {
    font-size: 14px;
  }

  .links-info {
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .link-rules,
  .my-blog-info {
    min-width: 100%;
    padding: 1rem;
  }

  .link-rules h3,
  .my-blog-info h3 {
    font-size: 1rem;
    margin-bottom: 0.8rem;
  }

  .link-rules ul {
    font-size: 0.85rem;
    padding-left: 0.8rem;
  }

  .my-card {
    flex-direction: row;
    align-items: center;
  }

  .my-card img {
    width: 55px;
    height: 55px;
  }

  .my-name {
    font-size: 0.95rem;
  }

  .my-desc,
  .my-url {
    font-size: 0.85rem;
  }

  .copy-button {
    font-size: 13px;
    padding: 0 8px;
    height: 22px;
    margin-left: 8px;
    margin-top: 0;
  }

  .button {
    width: 90px;
    height: 32px;
    font-size: 14px;
  }

  .links-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 0.8rem;
  }

  .link-card {
    padding: 1rem 0.8rem;
  }

  .avatar {
    width: 55px;
    height: 55px;
  }

  .name {
    font-size: 0.95rem;
    margin: 0.4rem 0;
  }

  .desc {
    font-size: 0.75rem;
  }

  /* 弹窗响应式 */
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

/* 手机竖屏 */
@media (max-width: 576px) {
  .links-container {
    padding: 10px 2%;
  }

  .links {
    padding: 12px 10px;
  }

  .links-header h2 {
    font-size: 22px;
  }

  .links-header p {
    font-size: 12px;
  }

  .divider {
    margin: 8px 0;
  }

  .links-info {
    gap: 0.8rem;
  }

  .link-rules,
  .my-blog-info {
    padding: 0.8rem;
  }

  .my-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .my-card img {
    width: 50px;
    height: 50px;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-label {
    margin-bottom: 2px;
  }

  .copy-button {
    margin-left: 0;
    margin-top: 6px;
  }

  .tooltip .tooltip-text {
    white-space: normal;
    width: 150px;
    font-size: 11px;
    bottom: 140%;
  }

  .button {
    width: 85px;
    height: 30px;
    font-size: 13px;
  }

  .links-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 0.6rem;
  }

  .link-card {
    padding: 0.8rem 0.5rem;
    border-radius: 10px;
  }

  .avatar {
    width: 48px;
    height: 48px;
  }

  .name {
    font-size: 0.85rem;
  }

  .desc {
    font-size: 0.7rem;
    line-height: 1.3;
  }
}

/* 超小屏幕 */
@media (max-width: 400px) {
  .links-grid {
    grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  }

  .avatar {
    width: 42px;
    height: 42px;
  }

  .name {
    font-size: 0.8rem;
  }

  .desc {
    font-size: 0.65rem;
  }
}
</style>
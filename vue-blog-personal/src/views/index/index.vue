<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { DArrowRight, ChatLineSquare, Document, View, Reading } from '@element-plus/icons-vue';
import { getPopularArticleListApi, getHomeNoticeApi } from '@/api/home.js';

const router = useRouter();

// 首页公告弹窗
const noticeVisible = ref(false);
const noticeData = ref({ title: '', content: '', updatedAt: '' });
// 关闭过的公告记录，避免同一则公告每次进首页都弹
const NOTICE_DISMISSED_KEY = 'homeNoticeDismissedAt';

const loadHomeNotice = async () => {
  try {
    const result = await getHomeNoticeApi();
    if (result.code !== 200 || !result.data || !result.data.content) return;
    const notice = result.data;
    if (localStorage.getItem(NOTICE_DISMISSED_KEY) === String(notice.updatedAt || '')) return;
    noticeData.value = notice;
    noticeVisible.value = true;
  } catch (err) {
    // 公告加载失败不影响首页其他内容
  }
};

// 关闭后记住这一则公告，管理员再次编辑（更新时间变化）后会重新弹出
const rememberNoticeDismissed = () => {
  localStorage.setItem(NOTICE_DISMISSED_KEY, String(noticeData.value.updatedAt || ''));
};

const articleList = ref([]);
const articleLoading = ref(true);
const layoutMode = ref(localStorage.getItem('blogLayout') || 'list');

const updateLayout = () => {
  layoutMode.value = localStorage.getItem('blogLayout') || 'list';
};

const hotTip = ref({ show: false, x: 0, y: 0, text: '' });

const showHotTip = (event, text) => {
  const rect = event.currentTarget.getBoundingClientRect();
  hotTip.value = {
    show: true,
    x: rect.left + rect.width / 2,
    y: rect.top - 8,
    text,
  };
};

const hideHotTip = () => {
  hotTip.value.show = false;
};

const getPopularArticleList = async () => {
  articleLoading.value = true;
  try {
    const result = await getPopularArticleListApi();
    if (result.code === 200 && Array.isArray(result.data)) {
      articleList.value = result.data.slice(0, 20) || [];
    }
  } catch (error) {
    articleList.value = [];
    console.error('获取首页热门文章异常', error);
  } finally {
    articleLoading.value = false;
  }
};

const goDetail = (id) => {
  router.push('/article/' + id);
};

onMounted(() => {
  window.addEventListener('layoutChange', updateLayout);
  getPopularArticleList();
  loadHomeNotice();
});

onUnmounted(() => {
  window.removeEventListener('layoutChange', updateLayout);
});
</script>

<template>
  <div class="common-index">
    <div class="quick-link-box">
      <div
        class="quick-btn"
        style="background-image: url('https://tu.ltyuanfang.cn/api/fengjing.php')"
        @click="router.push('/article')"
      >
        <div class="line-top"></div>
        <div class="line-left"></div>
        <div class="line-right"></div>
        <div class="line-bottom-left"></div>
        <div class="line-bottom-right"></div>
        <span
          ><el-icon> <Document /> </el-icon>文章列表<el-icon> <DArrowRight /> </el-icon
        ></span>
      </div>

      <div
        class="quick-btn"
        style="background-image: url('https://picsum.photos/1920/1080')"
        @click="router.push('/daily')"
      >
        <div class="line-top"></div>
        <div class="line-left"></div>
        <div class="line-right"></div>
        <div class="line-bottom-left"></div>
        <div class="line-bottom-right"></div>
        <span
          ><el-icon> <Reading /> </el-icon>日常分享<el-icon> <DArrowRight /> </el-icon
        ></span>
      </div>
    </div>

    <div class="popular-articles">
      <div class="header-row">
        <h2>热门文章</h2>
      </div>
      <div class="divider"></div>
      <div v-loading="articleLoading" class="article-list" :class="[layoutMode, { loading: articleLoading }]">
        <div class="article-item" v-for="item in articleList" :key="item.id" @click="goDetail(item.id)">
          <span v-if="item.isTop" class="top-icon">
            <span class="tooltip-inner">
              <font-awesome-icon icon="fa-solid fa-thumbtack" size="lg" :style="{ color: '#0090f0' }" />
              <span class="tooltip-text">置顶</span>
            </span>
          </span>
          <!-- 左侧图片 -->
          <div class="article-img">
            <img :src="item.cover" alt="文章封面" />
          </div>
          <!-- 右侧内容 -->
          <div class="article-content">
            <div class="article-meta">
              <img class="avatar" :src="item.userAvatar" alt="头像" />
              <span class="nickname">{{ item.userNickname }}</span>
              <span class="date">{{ item.createTime }}</span>
              <span class="category">{{ item.category }}</span>
            </div>
            <h3 class="article-title">
              {{ item.title }}
              <span
                v-if="item.isHot"
                class="hot-icon"
                @mouseenter="showHotTip($event, '热门文章')"
                @mouseleave="hideHotTip"
              >
                <font-awesome-icon icon="fa-solid fa-fire" size="xs" :style="{ color: '#ff5500' }" />
              </span>
            </h3>
            <p class="article-desc">{{ item.summary }}</p>
            <div class="article-stats">
              <span
                ><el-icon>
                  <View />
                </el-icon>
                {{ item.viewNum }}</span
              >
              <span><font-awesome-icon icon="fa-solid fa-thumbs-up" /> {{ item.likeNum }}</span>
              <span
                ><el-icon>
                  <ChatLineSquare />
                </el-icon>
                {{ item.commentNum }}</span
              >
              <span
                ><el-icon>
                  <Document />
                </el-icon>
                {{ item.wordsNum }}字</span
              >
            </div>
          </div>
        </div>
      </div>
      <div v-if="!articleLoading && articleList.length === 0" class="empty-data">暂无热门文章</div>
      <div v-show="hotTip.show" class="global-tooltip" :style="{ left: hotTip.x + 'px', top: hotTip.y + 'px' }">
        {{ hotTip.text }}
      </div>
    </div>

    <!-- 站点公告弹窗 -->
    <el-dialog
      v-model="noticeVisible"
      class="notice-dialog"
      width="480px"
      align-center
      :close-on-click-modal="false"
      @close="rememberNoticeDismissed"
    >
      <template #header>
        <h3 class="notice-dialog-title">{{ noticeData.title || '网站公告' }}</h3>
      </template>

      <div class="notice-dialog-content">{{ noticeData.content }}</div>

      <template #footer>
        <div class="notice-dialog-footer">
          <button type="button" class="notice-confirm-btn" @click="noticeVisible = false">我知道了</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.article-stats :deep(.el-icon) {
  font-size: 16px !important;
  margin-right: 4px;
  vertical-align: middle;
  position: relative;
  top: 0.1px;
}

.common-index {
  flex: 1;
  padding: 12px 12px;
  border-radius: 8px;
  background-color: var(--card-bg);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.notice-dialog-title {
  margin: 0;
  color: var(--text-color);
  font-size: 16px;
  font-weight: 600;
  line-height: 1.2;
}

.notice-dialog-content {
  max-height: 50vh;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.9;
  color: var(--text-main-color);
  white-space: pre-wrap;
  word-break: break-word;
  padding-right: 4px;
}

.notice-dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.notice-confirm-btn {
  padding: 7px 20px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #409eff, #003ae5);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notice-confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.35);
}

.quick-link-box {
  display: flex;
  gap: 16px;
  width: 100%;
  margin-bottom: 15px;
}

.quick-btn {
  flex: 1;
  height: 120px;
  border-radius: 12px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 遮罩层 */
.quick-btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1;
}

.quick-btn span {
  position: relative;
  z-index: 2;
  color: #fff;
  font-size: 36px;
  font-weight: 380;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

/* ----- 公共线条基座（白色） ----- */
.quick-btn .line-top,
.quick-btn .line-left,
.quick-btn .line-right,
.quick-btn .line-bottom-left,
.quick-btn .line-bottom-right {
  position: absolute;
  background: rgba(165, 255, 229, 0.808);
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 5;
}

/* 顶部横线（中间向两边） */
.quick-btn .line-top {
  top: 0;
  left: 50%;
  width: 0;
  height: 4px;
  transform: translateX(-50%);
}

/* 左侧竖线（上到下） */
.quick-btn .line-left {
  top: 0;
  left: 0;
  width: 4px;
  height: 0;
}

/* 右侧竖线（上到下） */
.quick-btn .line-right {
  top: 0;
  right: 0;
  width: 4px;
  height: 0;
}

/* 底部左侧线段（从左向右延伸至中间） */
.quick-btn .line-bottom-left {
  bottom: 0;
  left: 0;
  width: 0;
  height: 4px;
}

/* 底部右侧线段（从右向左延伸至中间） */
.quick-btn .line-bottom-right {
  bottom: 0;
  right: 0;
  width: 0;
  height: 4px;
}

/* hover 动画 */
.quick-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(243, 213, 108, 0.25);
}

/* 顶部动画：中间向两边扩展 */
.quick-btn:hover .line-top {
  opacity: 1;
  animation: lineExpandTop 0.3s ease forwards;
}

/* 左右动画：从上向下延伸（延迟 0.15s） */
.quick-btn:hover .line-left,
.quick-btn:hover .line-right {
  opacity: 1;
  animation: lineExpandSide 0.4s ease 0.15s forwards;
}

/* 底部左侧动画：从左向右延伸至 50%（汇合点） */
.quick-btn:hover .line-bottom-left {
  opacity: 1;
  animation: bottomLeftExpand 0.25s ease 0.45s forwards;
}

/* 底部右侧动画：从右向左延伸至 50%（汇合点） */
.quick-btn:hover .line-bottom-right {
  opacity: 1;
  animation: bottomRightExpand 0.25s ease 0.45s forwards;
}

/* 动画关键帧 */
@keyframes lineExpandTop {
  from {
    width: 0;
  }

  to {
    width: 100%;
  }
}

@keyframes lineExpandSide {
  from {
    height: 0;
  }

  to {
    height: 100%;
  }
}

/* 左侧底部线：从 0% → 50% 宽度（从左向右） */
@keyframes bottomLeftExpand {
  0% {
    width: 0;
    opacity: 0;
  }

  100% {
    width: 50%;
    opacity: 1;
  }
}

/* 右侧底部线：从 0% → 50% 宽度（从右向左） */
@keyframes bottomRightExpand {
  0% {
    width: 0;
    opacity: 0;
  }

  100% {
    width: 50%;
    opacity: 1;
  }
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.header-row h2 {
  margin: 2px 0;
  color: var(--text-color);
}

.divider {
  height: 1px;
  background: var(--border-color);
  margin-bottom: 20px;
}

/* 默认列表模式 */
.article-list {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-list.loading {
  min-height: 120px;
}

/* 双列网格 */
.article-list.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

/* 三列网格（田字格） */
.article-list.grid3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* 网格模式下：卡片改为垂直布局 + 自适应高度 */
.article-list.grid .article-item,
.article-list.grid3 .article-item {
  height: auto;
  flex-direction: column;
  padding: 0;
  gap: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 网格模式：图片高度 */
.article-list.grid .article-item .article-img,
.article-list.grid3 .article-item .article-img {
  width: 100%;
  height: 160px;
}

/* 网格模式：内容区域更紧凑 */
.article-list.grid .article-item .article-content,
.article-list.grid3 .article-item .article-content {
  padding: 12px;
  gap: 4px;
}

/* 标题缩小、间距收紧 */
.article-list.grid .article-item .article-title,
.article-list.grid3 .article-item .article-title {
  font-size: 18px;
  margin: 0 0 6px;
}

/* 摘要更小、更紧凑、行数更少 */
.article-list.grid .article-item .article-desc,
.article-list.grid3 .article-item .article-desc {
  font-size: 13px;
  line-height: 1.5;
  /* 稍微放大一点，避免文字贴边 */
  max-height: 3em;
  /* 改成 2 行完整高度：13px × 1.5 × 2 = 39px */
  margin: 0 0 8px;
  overflow: hidden;
  display: -webkit-box;
}

/* 元信息、统计信息缩小间距 */
.article-list.grid .article-item .article-meta,
.article-list.grid3 .article-item .article-meta,
.article-list.grid .article-item .article-stats,
.article-list.grid3 .article-item .article-stats {
  font-size: 12px;
  gap: 10px;
  margin-bottom: 6px;
}

/* 卡片统一动画 */
.article-item {
  transition: all 0.3s ease;
  /* 悬浮动画 */
  /* animation: fadeIn 0.4s ease forwards; */
  min-height: 200px;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 0px;
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  position: relative;
  /* 鼠标移上去变小手 */
}

/* 卡片悬浮上浮效果 */
.article-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(229, 199, 147, 0.426);
}

.top-icon {
  margin-top: 3px;
  position: absolute;
  top: 8px;
  right: 10px;
  z-index: 10;
  font-size: 18px;
}

.top-icon > :deep(.tooltip-inner) {
  position: relative;
  display: inline-flex;
}

.tooltip-text {
  visibility: hidden;
  opacity: 0;
  position: absolute;
  background: #303133;
  color: #ffffff;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 4px;
  white-space: nowrap;
  transition: 0.2s ease;
  z-index: 9999;
  bottom: 130%;
  left: 50%;
  transform: translateX(-50%);
  pointer-events: none;
}

.top-icon > :deep(.tooltip-inner):hover .tooltip-text {
  visibility: visible;
  opacity: 1;
}

.global-tooltip {
  position: fixed;
  z-index: 99999;
  transform: translate(-50%, -100%);
  background: #303133;
  color: #fff;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 4px;
  pointer-events: none;
  white-space: nowrap;
}

.hot-icon {
  display: inline-flex;
  margin-left: 6px;
}

/* 左侧图片容器 */
.article-img {
  width: 32%;
  height: 300px;
  overflow: hidden;
}

.article-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
  /* 图片放大动画 */
}

/* 图片悬浮放大 */
.article-item:hover .article-img img {
  transform: scale(1.08);
}

/* 右侧内容区 */
.article-content {
  flex: 1;
  /* 允许收缩到比内容更窄：标题不换行，否则会把卡片撑破 */
  min-width: 0;
  padding: 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary-color);
  font-size: 14px;
  margin-bottom: 12px;
}

.article-meta .avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}

.article-meta .nickname {
  color: var(--text-color);
}

.article-meta .date {
  margin: 0px 12px;
}

.article-meta .category {
  color: var(--primary-color);
  font-weight: 500;
}

.article-title {
  font-size: 26px;
  font-weight: 600;
  margin: 0 0 12px;
  color: var(--text-color);
  white-space: nowrap;
  /* 不换行 */
  overflow: hidden;
  text-overflow: ellipsis;
  /* 超出... */
}

.article-desc {
  font-size: 16px;
  color: var(--text-main-color);
  line-height: 1.6;
  margin: 0 0 16px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-stats {
  display: flex;
  gap: 20px;
  color: var(--text-secondary-color);
  font-size: 14px;
}

.empty-data {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary-color);
  font-size: 16px;
  padding: 60px 0;
}

/* ========== 快捷按钮（文章列表 / 日常分享）响应式 ==========
   两个按钮在同一行平分宽度，屏幕越窄每个按钮越窄；
   字号不跟着降，文字就会折成多行，超出按钮高度的部分会被 overflow:hidden 裁掉 */
/* 侧边栏布局下中栏被两侧栏挤到 500 多像素，两个按钮各只剩 200~280px，
   36px 的字会折行（931px 附近按钮只有 199px），这里统一降一档 */
@media (max-width: 1100px) {
  .quick-btn span {
    font-size: 26px;
  }
}

@media (max-width: 600px) {
  /* 高度改为自适应：即使文字折行也不会被裁掉，两个按钮仍然等高 */
  .quick-btn {
    height: auto;
    min-height: 88px;
    padding: 14px 6px;
  }

  .quick-btn span {
    font-size: 20px;
    gap: 6px;
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
  }
}

@media (max-width: 400px) {
  .quick-btn span {
    font-size: 16px;
    gap: 4px;
  }
}
</style>

<!--
  el-dialog 会渲染到组件作用域之外，scoped 的 :deep() 拿不到它的内部节点，
  所以弹窗外观覆写放在这个非 scoped 块里，用 .notice-dialog 限定作用范围。
-->
<style>
.el-dialog.notice-dialog {
  max-width: calc(100vw - 40px);
  padding: 20px 22px 18px;
  border-radius: 12px;
  background-color: var(--card-secound-bg);
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
}

.notice-dialog .el-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 0 16px;
  padding: 0;
}

.notice-dialog .el-dialog__headerbtn {
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

.notice-dialog .el-dialog__headerbtn:hover {
  background-color: var(--card-bg-hover);
  color: var(--hover-color);
}

.notice-dialog .el-dialog__close {
  color: inherit;
  font-size: 14px;
}

.notice-dialog .el-dialog__body {
  padding: 0;
  color: var(--text-color);
}

.notice-dialog .el-dialog__footer {
  padding: 0;
  margin-top: 18px;
}
</style>

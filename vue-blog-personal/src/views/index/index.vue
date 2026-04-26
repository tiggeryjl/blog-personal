<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { DArrowRight, ChatLineSquare, Document, View, Reading } from '@element-plus/icons-vue'
import WelcomeBanner from '@/components/WelcomeBanner.vue'

const router = useRouter()

// 公告内容（你自己随便改）
const notice = ref(`【公告】
欢迎来到我的个人博客！
这里会记录技术文章、日常感悟、学习笔记~

后续更新计划：
▫️ 完善登录注册功能
▫️ 新增日常发布按钮
▫️ 优化页面样式
▫️ 增加设置页面`)

const articleList = ref([
  {
    id: 1,
    title: "新起点 新动力！",
    desc: "这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...",
    category: "心得",
    date: "2026-02-21 14:03",
    cover: "https://picsum.photos/400/250",
    view: 636,
    like: 7,
    comment: 12,
    words: 1077
  },
  {
    id: 2,
    title: "新起点 新动力！",
    desc: "这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...",
    category: "心得",
    date: "2026-02-21 14:03",
    cover: "https://picsum.photos/400/250",
    view: 636,
    like: 7,
    comment: 12,
    words: 1077
  },
  {
    id: 3,
    title: "新起点 新动力！",
    desc: "这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...",
    category: "心得",
    date: "2026-02-21 14:03",
    cover: "https://picsum.photos/400/250",
    view: 636,
    like: 7,
    comment: 12,
    words: 1077
  },
  {
    id: 4,
    title: "新起点 新动力！",
    desc: "这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...",
    category: "心得",
    date: "2026-02-21 14:03",
    cover: "https://picsum.photos/400/250",
    view: 636,
    like: 7,
    comment: 12,
    words: 1077
  },
  {
    id: 5,
    title: "新起点 新动力！",
    desc: "这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...",
    category: "心得",
    date: "2026-02-21 14:03",
    cover: "https://picsum.photos/400/250",
    view: 636,
    like: 7,
    comment: 12,
    words: 1077
  },
  {
    id: 6,
    title: "新起点 新动力！",
    desc: "这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...这是文章摘要内容，用来预览文章的核心观点...",
    category: "心得",
    date: "2026-02-21 14:03",
    cover: "https://picsum.photos/400/250",
    view: 636,
    like: 7,
    comment: 12,
    words: 1077
  },
])

const goDetail = (id) => {
  router.push('/article/' + id)
}
</script>

<template>
  <div class="common-index">

    <!-- 公告卡片（占满父级宽度） -->
    <div class="notice-card">
      <h3 class="notice-title">📢 网站公告</h3>
      <div class="notice-content">{{ notice }}</div>
    </div>

    <!-- 两个快捷按钮（同一行平分宽度） -->
    <div class="quick-link-box">
      <div class="quick-btn" style="background-image: url('https://picsum.photos/800/400?random=1')"
        @click="router.push('/article')">
        <div class="line-top"></div>
        <div class="line-left"></div>
        <div class="line-right"></div>
        <div class="line-bottom-left"></div>
        <div class="line-bottom-right"></div>
        <span><el-icon>
            <Document />
          </el-icon>文章列表<el-icon>
            <DArrowRight />
          </el-icon></span>
      </div>

      <div class="quick-btn" style="background-image: url('https://picsum.photos/800/400?random=2')"
        @click="router.push('/daily')">
        <div class="line-top"></div>
        <div class="line-left"></div>
        <div class="line-right"></div>
        <div class="line-bottom-left"></div>
        <div class="line-bottom-right"></div>
        <span><el-icon>
            <Reading />
          </el-icon>日常分享<el-icon>
            <DArrowRight />
          </el-icon></span>
      </div>
    </div>

    <div>
      <h2> 热门文章 </h2>
      <div class="divider"></div>
      <div class="article-list">
        <div class="article-item" v-for="item in articleList" :key="item.id" @click="goDetail(item.id)">
          <!-- 左侧图片 -->
          <div class="article-img">
            <img :src="item.cover" alt="文章封面" />
          </div>
          <!-- 右侧内容 -->
          <div class="article-content">
            <div class="article-meta">
              <span class="category">{{ item.category }}</span>
              <span class="date">{{ item.date }}</span>
            </div>
            <h3 class="article-title">{{ item.title }}</h3>
            <p class="article-desc">{{ item.desc }}</p>
            <div class="article-stats">
              <span><el-icon>
                  <View />
                </el-icon> {{ item.view }}</span>
              <span><font-awesome-icon icon="fa-solid fa-thumbs-up" /> {{ item.like }}</span>
              <span><el-icon>
                  <ChatLineSquare />
                </el-icon> {{ item.comment }}</span>
              <span><el-icon>
                  <Document />
                </el-icon> {{ item.words }}字</span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="articleList.length === 0" class="empty-data">
        暂无文章数据
      </div>
    </div>

  </div>
</template>

<style scoped>
.common-index {
  flex: 1;
  padding: 12px 12px;
  border-radius: 8px;
  background-color: var(--card-bg);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 公告卡片 */
.notice-card {
  width: 100%;
  padding: 20px;
  background-color: var(--card-bg);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.notice-title {
  margin: 0 0 12px 0;
  font-size: 28px;
  color: var(--text-color);
}

.notice-content {
  color: var(--text-main-color);
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
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
  content: "";
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

.header-row h2 {
  margin: 0 0 8px;
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
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  /* 鼠标移上去变小手 */
}

/* 卡片悬浮上浮效果 */
.article-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(229, 199, 147, 0.426);
}

/* 左侧图片容器 */
.article-img {
  width: 32%;
  height: 100%;
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
  padding: 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-meta {
  display: flex;
  gap: 16px;
  color: var(--text-secondary-color);
  font-size: 14px;
  margin-bottom: 12px;
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
</style>
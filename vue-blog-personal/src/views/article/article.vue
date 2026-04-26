<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'
import { Expand, Menu, Grid, Edit, ChatLineSquare, Document, View } from '@element-plus/icons-vue'

const router = useRouter()
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

const goEdit = () => {
  router.push('/editInput')
}
const goDetail = (id) => {
  router.push('/article/' + id)
}

const layoutMode = ref(localStorage.getItem('blogLayout') || 'list')
const showDropdown = ref(false)
const layoutIcons = {
  list: Expand,
  grid: Menu,
  grid3: Grid
}


const changeLayout = (mode) => {
  layoutMode.value = mode
  localStorage.setItem('blogLayout', mode)
  window.dispatchEvent(new Event('layoutChange'))
  showDropdown.value = false
}

const updateLayout = () => {
  layoutMode.value = localStorage.getItem('blogLayout') || 'list'
}

const closeDropdown = (e) => {
  const dropdown = document.querySelector('.layout-dropdown')
  if (dropdown && !dropdown.contains(e.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  window.addEventListener('layoutChange', updateLayout)
  window.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  window.removeEventListener('layoutChange', updateLayout)
  window.removeEventListener('click', closeDropdown)
})

</script>

<template>
  <div class="common-article">

    <div class="fixed-header">
      <div class="header-row">
        <div class="publish-box" v-if="false">
          <!-- 你的发布输入框代码 -->
        </div>
        <h2>文章</h2>
        <div class="layout-dropdown">
          <button class="layout-trigger" @click="goEdit()">
            <span class="icon-wrap">
              <el-icon>
                <Edit />
              </el-icon>
            </span>发布文章
          </button>
          <button class="layout-trigger" @click="showDropdown = !showDropdown">
            <span class="icon-wrap">
              <component :is="layoutIcons[layoutMode]" />
            </span>页面布局
          </button>
          <div class="dropdown-menu" v-show="showDropdown">
            <div class="dropdown-arrow"></div>
            <div class="dropdown-item" @click="changeLayout('list')">
              <span class="icon-wrap">
                <component :is="Expand" />
              </span> 列表
            </div>
            <div class="dropdown-item" @click="changeLayout('grid')">
              <span class="icon-wrap">
                <component :is="Menu" />
              </span> 网格
            </div>
            <div class="dropdown-item" @click="changeLayout('grid3')">
              <span class="icon-wrap">
                <component :is="Grid" />
              </span> 三列
            </div>
          </div>
        </div>
      </div>
      <div class="divider"></div>
    </div>

    <div class="article-list" :class="layoutMode">
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
</template>

<style scoped>
.article-stats :deep(.el-icon) {
  font-size: 16px !important;
  margin-right: 4px;
  vertical-align: middle;
  position: relative;
  top: 0.1px;
}


.common-article {
  padding: 5px 12px;
  border-radius: 8px;
  background-color: var(--card-bg);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}


/* 🔒 固定头部区域：永远保持原样 */
.fixed-header {
  flex-shrink: 0;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.header-row h2 {
  margin: 0 0 8px;
  color: var(--text-color);
}

.layout-dropdown {
  position: relative;
  display: inline-block;
}

.layout-trigger {
  background-color: var(--card-bg);
  border: none;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-color);
  cursor: pointer;
  padding: 4px 8px;
  margin: 0px 5px;
  border-radius: 16px;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  white-space: nowrap;
}

.layout-trigger:hover {
  color: var(--hover-color);
  background-color: var(--card-bg-hover);
}

.icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
}

.icon-wrap svg {
  width: 100% !important;
  height: 100% !important;
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 6px;
  background: var(--card-secound-bg);
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 4px 0;
  min-width: 100px;
  z-index: 99;
}

.dropdown-arrow {
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 6px solid var(--card-secound-bg);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  color: var(--text-color);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 6px;
  margin: 0 4px;
}

.dropdown-item:hover {
  background: var(--card-bg-hover);
  color: var(--hover-color);
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
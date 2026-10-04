<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Expand, Menu, Grid, ChatLineSquare, Document, View, Back } from '@element-plus/icons-vue';
import MyPagination from '@/components/MyPagination.vue';
import { getArticleListApi } from '@/api/article.js';
import { getCategoryListApi } from '@/api/category.js';

const route = useRoute();
const router = useRouter();

// 当前筛选的分类 id
const categoryId = computed(() => {
  const id = route.query.categoryId;
  if (id == null || id === '') return '';
  return String(Array.isArray(id) ? id[0] : id);
});

// 分类 id -> 分类名，用于在标题处展示当前选中的分类
const categoryMap = ref({});
const categoryName = computed(() => categoryMap.value[categoryId.value] || '');

// 当前筛选的标签名
const tag = computed(() => {
  const value = route.query.tag;
  if (value == null || value === '') return '';
  return String(Array.isArray(value) ? value[0] : value);
});

// 当前筛选的日期
const date = computed(() => {
  const value = route.query.date;
  if (value == null || value === '') return '';
  const raw = String(Array.isArray(value) ? value[0] : value);
  // 只接受 yyyy-MM-dd，避免非法参数拼出错误的查询条件
  return /^\d{4}-\d{2}-\d{2}$/.test(raw) ? raw : '';
});

// 格式化日期 2026年10月1日
const dateText = computed(() => date.value.replace(/^(\d{4})-(\d{2})-(\d{2})$/, '$1年$2月$3日'));

const getCategoryMap = async () => {
  try {
    const result = await getCategoryListApi();
    if (result.code === 200 && Array.isArray(result.data)) {
      categoryMap.value = result.data.reduce((map, item) => {
        map[String(item.id)] = item.name;
        return map;
      }, {});
    }
  } catch (error) {
    console.error('获取分类列表异常', error);
  }
};

// 返回全部文章：清除分类 / 标签筛选条件
const goAllArticles = () => {
  router.push('/article');
};

const articleList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const getArticleList = async () => {
  try {
    const result = await getArticleListApi({
      page: currentPage.value,
      pageSize: pageSize.value,
      categoryId: categoryId.value || undefined,
      tag: tag.value || undefined,
      // 只筛选中选的这一天：00:00:00 ~ 23:59:59
      begin: date.value ? `${date.value} 00:00:00` : undefined,
      end: date.value ? `${date.value} 23:59:59` : undefined,
    });
    if (result.code === 200) {
      articleList.value = result.data.rows;
      total.value = result.data.total;
    }
  } catch (error) {
    console.error('获取文章列表异常', error);
  }
};

const goDetail = (id) => {
  router.push('/article/' + id);
};

const layoutMode = ref(localStorage.getItem('blogLayout') || 'list');
const showDropdown = ref(false);
const layoutIcons = {
  list: Expand,
  grid: Menu,
  grid3: Grid,
};

const changeLayout = (mode) => {
  layoutMode.value = mode;
  localStorage.setItem('blogLayout', mode);
  window.dispatchEvent(new Event('layoutChange'));
  showDropdown.value = false;
};

const updateLayout = () => {
  layoutMode.value = localStorage.getItem('blogLayout') || 'list';
};

const closeDropdown = (e) => {
  const dropdown = document.querySelector('.layout-dropdown');
  if (dropdown && !dropdown.contains(e.target)) {
    showDropdown.value = false;
  }
};

const hotTip = ref({
  show: false,
  x: 0,
  y: 0,
  text: '',
});

const showHotTip = (e, text) => {
  const rect = e.target.getBoundingClientRect();
  hotTip.value.text = text;
  hotTip.value.show = true;
  hotTip.value.x = rect.left + rect.width / 2;
  hotTip.value.y = rect.top - 8;
};
const hideHotTip = () => {
  hotTip.value.show = false;
};

// 筛选条件变化时（切换分类 / 切换标签 / 返回全部文章），回到第一页并重新查询
watch(
  () => [route.query.categoryId, route.query.tag, route.query.date],
  () => {
    currentPage.value = 1;
    getArticleList();
  }
);

onMounted(() => {
  window.addEventListener('layoutChange', updateLayout);
  window.addEventListener('click', closeDropdown);
  getCategoryMap();
  getArticleList();
});

onUnmounted(() => {
  window.removeEventListener('layoutChange', updateLayout);
  window.removeEventListener('click', closeDropdown);
});
</script>

<template>
  <div class="common-article">
    <div class="fixed-header">
      <div class="header-row">
        <div class="header-left">
          <h2 v-if="categoryId">分类：{{ categoryName || '加载中…' }}</h2>
          <h2 v-else-if="tag">标签：{{ tag }}</h2>
          <h2 v-else-if="date">日期：{{ dateText }}</h2>
          <h2 v-else>全部文章</h2>
          <button v-if="categoryId || tag || date" class="clear-filter-btn" @click="goAllArticles">
            <el-icon><Back /></el-icon>
            全部文章
          </button>
        </div>
        <div class="layout-dropdown">
          <button class="layout-trigger" @click="showDropdown = !showDropdown">
            <span class="icon-wrap"> <component :is="layoutIcons[layoutMode]" /> </span>页面布局
          </button>
          <div class="dropdown-menu" v-show="showDropdown">
            <div class="dropdown-arrow"></div>
            <div class="dropdown-item" @click="changeLayout('list')">
              <span class="icon-wrap">
                <component :is="Expand" />
              </span>
              列表
            </div>
            <div class="dropdown-item" @click="changeLayout('grid')">
              <span class="icon-wrap">
                <component :is="Menu" />
              </span>
              网格
            </div>
            <div class="dropdown-item" @click="changeLayout('grid3')">
              <span class="icon-wrap">
                <component :is="Grid" />
              </span>
              三列
            </div>
          </div>
        </div>
      </div>
      <div class="divider"></div>
    </div>

    <div class="article-list" :class="layoutMode">
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
    <div v-if="articleList.length === 0" class="empty-data">
      {{ categoryId ? '该分类下暂无文章' : tag ? '该标签下暂无文章' : date ? '该日期下暂无文章' : '暂无文章数据' }}
    </div>
    <div v-show="hotTip.show" class="global-tooltip" :style="{ left: hotTip.x + 'px', top: hotTip.y + 'px' }">
      {{ hotTip.text }}
    </div>
    <div>
      <!-- 分页 -->
      <MyPagination
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
        @change="getArticleList()"
      />
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
  margin: 2px 0;
  color: var(--text-color);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.header-left h2 {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.clear-filter-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  padding: 3px 12px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background-color: var(--card-bg);
  color: var(--text-secondary-color);
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.25s ease;
}

.clear-filter-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
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

/* 三列网格 */
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
  min-height: 200px;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 0px;
  background-color: var(--card-bg);
  border-radius: 8px;
  /* overflow: hidden; */
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer; /* 鼠标移上去变小手 */
  position: relative;
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
/* tooltip气泡通用 */
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
  white-space: nowrap; /* 不换行 */
  overflow: hidden;
  text-overflow: ellipsis; /* 超出... */
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

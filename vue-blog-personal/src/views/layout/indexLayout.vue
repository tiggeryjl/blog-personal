<script setup>
import { useRouter, useRoute } from 'vue-router';
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import WelcomeBanner from '@/components/WelcomeBanner.vue';
import { ElMessage } from 'element-plus';
import { getPersonalInfoApi } from '@/api/auth.js';
import { getCategoryListApi } from '@/api/category.js';
import { getTagListApi } from '@/api/tag.js';

const route = useRoute();

const userProfile = ref({
  id: '',
  nickname: '',
  avatar: '',
  intro: '',
  github: '',
  email: '',
});

const getPersonalInfo = async () => {
  try {
    const response = await getPersonalInfoApi();
    userProfile.value = response.data;
  } catch (error) {
    console.error('获取个人信息失败:', error);
  }
};

// 搜索框关键词
const searchKeyword = ref('');

const search = () => {
  console.log('搜索关键词:', searchKeyword.value);
  ElMessage.info(`搜索功能暂未实现，关键词: ${searchKeyword.value}`);
};

const categoryList = ref([]);

const getCategoryList = async () => {
  try {
    const result = await getCategoryListApi();
    categoryList.value = result.data;
  } catch (error) {
    console.error('获取分类列表失败:', error);
  }
};

const tagList = ref([]);

const getTagList = async () => {
  try {
    const result = await getTagListApi();
    tagList.value = result.data;
    await rescanTagOverflow();
  } catch (error) {
    console.error('获取标签列表失败:', error);
  }
};

// 站点信息统计（当前为前端展示占位，后续接入后端接口后直接赋值）
const siteStats = ref({
  onlineVisitor: 0,
  todayView: 0,
  totalTraffic: 0,
  totalVisitor: 0,
});
const runningTimeText = ref('--');

// 默认展示分类排列，点击右上角按钮切换成标签排列
const viewMode = ref('category');
const moreDialogVisible = ref(false);
const hiddenTagCount = ref(0);
const visibleTagCount = ref(null);
const tagAreaRef = ref(null);

// 标签云始终展示独立的全量标签列表，固定高度内只展示能完整放下的部分
const displayTags = computed(() => {
  if (visibleTagCount.value == null) return tagList.value;
  return tagList.value.slice(0, visibleTagCount.value);
});

// 标签配色：根据名称稳定取色，不同标签颜色有区分
const tagColorPalette = [
  '#1677ff',
  '#eb2f96',
  '#fa8c16',
  '#13c2c2',
  '#722ed1',
  '#52c41a',
  '#f5222d',
  '#2f54eb',
  '#d46b08',
  '#08979c',
];

const hashTagName = (name) => {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) >>> 0;
  }
  return hash;
};

const tagColorStyle = (name) => {
  const color = tagColorPalette[hashTagName(name) % tagColorPalette.length];
  return {
    color,
    borderColor: `${color}66`,
    backgroundColor: `${color}17`,
  };
};

// 右上角按钮：在“分类排列”和“标签排列”之间切换
const toggleViewMode = async () => {
  if (viewMode.value === 'category') {
    viewMode.value = 'tag';
    await rescanTagOverflow();
  } else {
    viewMode.value = 'category';
    moreDialogVisible.value = false;
  }
};

// 测量固定高度区域内能完整放下多少个标签
const scanTagOverflow = () => {
  const area = tagAreaRef.value;
  if (!area) {
    hiddenTagCount.value = 0;
    visibleTagCount.value = null;
    return;
  }
  const areaBottom = area.getBoundingClientRect().bottom;
  let visible = 0;
  area.querySelectorAll('.tag-chip').forEach((chip) => {
    if (chip.getBoundingClientRect().bottom <= areaBottom + 1) visible += 1;
  });
  visibleTagCount.value = visible;
  hiddenTagCount.value = Math.max(0, tagList.value.length - visible);
};

// 切换视图或窗口尺寸变化后重新完整渲染测量一次
const rescanTagOverflow = async () => {
  visibleTagCount.value = null;
  await nextTick();
  scanTagOverflow();
};

const remainingTags = computed(() => {
  const hiddenCount = hiddenTagCount.value;
  if (hiddenCount <= 0) return [];
  return tagList.value.slice(Math.max(0, tagList.value.length - hiddenCount));
});

const today = new Date();
const currentYear = ref(today.getFullYear());
const currentMonth = ref(today.getMonth() + 1);
const selectedDate = ref(null);

// 点击日期
const selectDay = (day) => {
  if (day.isOtherMonth) return;
  selectedDate.value = {
    year: currentYear.value,
    month: currentMonth.value,
    date: day.date,
    isToday: day.isToday,
  };
};

// 回到今天
const goToday = () => {
  currentYear.value = today.getFullYear();
  currentMonth.value = today.getMonth() + 1;
  selectedDate.value = null;
};

// 上一个月
const prevMonth = () => {
  currentMonth.value--;
  if (currentMonth.value < 1) {
    currentMonth.value = 12;
    currentYear.value--;
  }
  selectedDate.value = null;
};

// 下一个月
const nextMonth = () => {
  currentMonth.value++;
  if (currentMonth.value > 12) {
    currentMonth.value = 1;
    currentYear.value++;
  }
  selectedDate.value = null;
};

// 生成日历数据
const calendarDays = computed(() => {
  const year = currentYear.value;
  const month = currentMonth.value - 1;
  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const daysInPrevMonth = new Date(year, month, 0).getDate();

  const days = [];

  // 上月
  for (let i = 0; i < firstDay; i++) {
    days.push({
      date: daysInPrevMonth - firstDay + i + 1,
      isOtherMonth: true,
      isToday: false,
    });
  }

  // 当月
  for (let i = 1; i <= daysInMonth; i++) {
    const isToday = i === today.getDate() && month === today.getMonth() && year === today.getFullYear();

    days.push({
      date: i,
      isOtherMonth: false,
      isToday: isToday,
    });
  }

  // 下月补全
  const remain = 42 - days.length;
  for (let i = 1; i <= remain; i++) {
    days.push({
      date: i,
      isOtherMonth: true,
      isToday: false,
    });
  }

  return days;
});

const show = computed(() => {
  return route.path === '/' || route.path.startsWith('/index');
});

onMounted(async () => {
  getPersonalInfo();
  getCategoryList();
  getTagList();
  await rescanTagOverflow();
  window.addEventListener('resize', rescanTagOverflow);
  if (document.fonts?.ready) {
    document.fonts.ready.then(() => rescanTagOverflow());
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', rescanTagOverflow);
});
</script>

<template>
  <div v-if="show">
    <WelcomeBanner />
  </div>

  <div class="blog-layout">
    <!-- 左侧侧边栏 -->
    <aside class="sidebar">
      <!-- 1. 个人信息区域 -->
      <div class="profile-card">
        <img :src="userProfile.avatar" alt="头像" class="avatar" />
        <h3 class="nickname">{{ userProfile.nickname }}</h3>
        <p class="intro">{{ userProfile.intro }}</p>

        <div class="stats">
          <div class="stats-item">
            <span>文章</span>
            <span>{{ 0 }}</span>
          </div>
          <div class="stats-item">
            <span>分类</span>
            <span>{{ categoryList.length }}</span>
          </div>
          <div class="stats-item">
            <span>标签</span>
            <span>{{ tagList.length }}</span>
          </div>
        </div>
        <div class="links">
          <a :href="userProfile.github" target="_blank" title="GitHub">
            <font-awesome-icon :icon="['fab', 'github']" />
          </a>
          <a :href="`mailto:${userProfile.email}`" target="_blank" title="Email">
            <font-awesome-icon icon="envelope" />
          </a>
          <a target="_blank" title="RSS订阅">
            <font-awesome-icon icon="rss" />
          </a>
        </div>
      </div>

      <div class="category-card search-card">
        <div class="search-wrapper">
          <el-icon class="search-icon" @click="search()"><Search /></el-icon>
          <input v-model="searchKeyword" type="text" placeholder="搜索文章..." class="search-input" />
          <el-icon v-if="searchKeyword" class="search-clear" @click="searchKeyword = ''">
            <Close />
          </el-icon>
        </div>
      </div>

      <!-- 分类导航：默认分类列表，右上角按钮切换标签云 -->
      <div class="category-card category-nav-card">
        <div class="category-nav-head">
          <h4>
            <el-icon>
              <Management />
            </el-icon>
            分类标签
          </h4>

          <button type="button" class="category-view-toggle" @click="toggleViewMode">
            <font-awesome-icon
              :icon="viewMode === 'category' ? 'tags' : 'folder-open'"
              class="category-view-toggle-icon"
            />
            {{ viewMode === 'category' ? '查看标签' : '查看分类' }}
          </button>
        </div>

        <!-- 默认：分类排列，一行展示一个分类 -->
        <div v-if="viewMode === 'category'" class="category-mode-list">
          <div v-for="category in categoryList" :key="category.id" class="category-row">
            <span class="category-row-dot"></span>
            <span class="category-row-name">{{ category.name }}</span>
            <span class="category-row-count">{{ category.articleCount }} 篇</span>
          </div>
        </div>

        <!-- 标签排列：保持现有的彩色标签云布局 -->
        <template v-else>
          <div ref="tagAreaRef" class="tag-area">
            <span v-for="tag in displayTags" :key="tag.id" class="tag-chip" :style="tagColorStyle(tag.name)">
              {{ tag.name }}
            </span>
          </div>

          <div class="tag-more-row">
            <button v-if="hiddenTagCount > 0" type="button" class="tag-more-btn" @click="moreDialogVisible = true">
              更多 {{ hiddenTagCount }} 个
              <el-icon class="more-arrow"><DArrowRight /></el-icon>
            </button>
          </div>
        </template>
      </div>

      <!-- 美化版日历组件 -->
      <div class="category-card">
        <div class="calendar-header">
          <h4><font-awesome-icon icon="calendar" size="lg" /> 日历</h4>
          <div class="calendar-nav">
            <el-icon class="arrow today-btn" @click="goToday" style="margin-left: 6px">
              <RefreshRight />
            </el-icon>
            <el-icon class="arrow" @click="prevMonth">
              <ArrowLeft />
            </el-icon>
            <span>{{ currentYear }}年{{ currentMonth }}月</span>
            <el-icon class="arrow" @click="nextMonth">
              <ArrowRight />
            </el-icon>
          </div>
        </div>
        <div class="calendar">
          <div class="calendar-week">
            <span>日</span><span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span>
          </div>
          <div class="calendar-days">
            <span
              v-for="(day, index) in calendarDays"
              :key="index"
              :class="{
                'calendar-day': true,
                'other-month': day.isOtherMonth,
                today: day.isToday,
                selected:
                  selectedDate !== null &&
                  selectedDate?.year === currentYear &&
                  selectedDate?.month === currentMonth &&
                  selectedDate?.date === day.date &&
                  !day.isOtherMonth,
              }"
              @click="selectDay(day)"
            >
              {{ day.date }}
            </span>
          </div>
        </div>
      </div>

      <div class="category-card">
        <h4>
          <el-icon>
            <Comment />
          </el-icon>
          站点信息统计
        </h4>

        <div class="site-stat-grid">
          <div class="site-stat-item">
            <span class="site-stat-label">在线访客</span>
            <span class="site-stat-value">{{ siteStats.onlineVisitor }}</span>
          </div>
          <div class="site-stat-item">
            <span class="site-stat-label">今日浏览</span>
            <span class="site-stat-value">{{ siteStats.todayView }}</span>
          </div>
          <div class="site-stat-item">
            <span class="site-stat-label">总浏览量</span>
            <span class="site-stat-value">{{ siteStats.totalTraffic }}</span>
          </div>
          <div class="site-stat-item">
            <span class="site-stat-label">总访问量</span>
            <span class="site-stat-value">{{ siteStats.totalVisitor }}</span>
          </div>
          <div class="site-stat-uptime">
            <span class="site-stat-label">运行时长</span>
            <span class="site-stat-uptime-value">{{ runningTimeText }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧主内容区 -->
    <main>
      <router-view />
    </main>
  </div>

  <el-dialog
    v-model="moreDialogVisible"
    class="more-tags-dialog"
    :title="`更多标签（${hiddenTagCount}）`"
    width="400px"
    align-center
  >
    <div class="dialog-tag-cloud">
      <span v-for="tag in remainingTags" :key="tag.id" class="tag-chip" :style="tagColorStyle(tag.name)">
        {{ tag.name }}
      </span>
      <span v-if="remainingTags.length === 0" class="dialog-tag-empty">暂无更多标签</span>
    </div>
  </el-dialog>
</template>

<style scoped>
.blog-layout {
  display: flex;
  gap: 30px;
  /* 左右栏间距 */
  padding: 20px 9%;
  min-height: calc(100vh - 65px);
  /* 减去导航栏高度 */
}

/* 左侧侧边栏 */
.sidebar {
  width: 300px;
  /* 固定宽度 */
  flex-shrink: 0;
  align-self: flex-start;
  position: sticky;
  top: 90px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 个人信息卡片 */
.profile-card {
  height: auto;
  background-color: var(--card-bg);
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 8px;
  border: 2px solid var(--primary-color);
}

.nickname {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-color);
}

.intro {
  margin: 0 0 5px;
  font-size: 14px;
  color: var(--text-secondary-color);
  line-height: 1.5;
}

/* 文章 | 分类 统计 —— 上下排列核心 */
.profile-card .stats {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.profile-card .stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--text-color);
  font-size: 14px;
}

.profile-card .stats-item span:last-child {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-data-prompt-color);
  margin-top: 4px;
}

.profile-card .links {
  display: flex;
  gap: 18px;
  margin-top: 6px;
}

.profile-card .links a {
  display: inline-flex;
  align-items: center;
  color: var(--text-color);
  text-decoration: none;
  font-size: 21px;
  transition: color 0.3s;
  margin: 0px 5px;
}

.profile-card .links a:hover {
  color: var(--hover-color);
}

/* 分类/公告卡片 */
.category-card {
  background-color: var(--card-bg);
  padding: 20px;
  border-radius: 8px;
}

.category-card h4 :deep(.el-icon) {
  font-size: 24px !important;
  vertical-align: middle;
  position: relative;
}

.category-card h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: var(--text-color);
}

/* 搜索卡片 */
.search-card {
  padding: 12px 14px;
}

.search-wrapper {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 10px 0 14px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: var(--card-secound-bg);
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.search-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.16);
}

.search-icon {
  flex-shrink: 0;
  margin-right: 8px;
  font-size: 17px;
  color: var(--text-secondary-color);
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-color);
  font-size: 14px;
}

.search-input::placeholder {
  color: var(--text-prompt-color);
}

.search-clear {
  flex-shrink: 0;
  margin-left: 6px;
  padding: 2px;
  border-radius: 50%;
  font-size: 14px;
  color: var(--text-prompt-color);
  cursor: pointer;
  transition: color 0.2s;
}

.search-clear:hover {
  color: var(--primary-color);
}

/* 分类导航卡片 */
.category-nav-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.category-nav-head h4 {
  margin: 0;
}

.category-view-toggle {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  max-width: 112px;
  padding: 5px 12px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: var(--card-secound-bg);
  color: var(--text-color);
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: border-color 0.25s, color 0.25s;
}

.category-view-toggle:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.category-view-toggle-icon {
  flex-shrink: 0;
  font-size: 13px;
}

/* 分类排列：固定高度，一行一个分类，超出后内部滚动 */
.category-mode-list {
  height: 152px;
  overflow-y: auto;
  scrollbar-width: none;
}

.category-mode-list::-webkit-scrollbar {
  display: none;
}

.category-row {
  display: flex;
  align-items: center;
  padding: 8px 2px;
  /* border-bottom: 1px solid var(--border-color); */
}

.category-row:last-child {
  border-bottom: none;
}

.category-row-dot {
  flex-shrink: 0;
  width: 6px;
  height: 6px;
  margin-right: 10px;
  border-radius: 50%;
  background: var(--primary-color);
}

.category-row-name {
  flex: 1;
  font-size: 14px;
}

.category-row-count {
  margin-right: 6px;
  font-size: 12px;
  color: var(--text-prompt-color);
}

/* 标签区域：固定高度，标签按行换行展示 */
.tag-area {
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 8px;
  height: 122px;
  overflow: hidden;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border: 1px solid;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1;
  white-space: nowrap;
  user-select: none;
  cursor: pointer;
  transition: transform 0.2s, filter 0.2s, box-shadow 0.2s;
}

.tag-chip:hover {
  filter: brightness(1.08);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

/* 更多标签入口行：预留固定高度，卡片高度不跳动 */
.tag-more-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 24px;
  margin-top: 6px;
}

.tag-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--primary-color);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
}

.tag-more-btn:hover {
  color: var(--hover-color);
}

.more-arrow {
  font-size: 13px;
}

/* 更多标签弹窗 */
.dialog-tag-cloud {
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 8px;
}

.dialog-tag-empty {
  color: var(--text-prompt-color);
  font-size: 13px;
}

/* 日历卡片 */
.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* 月份切换导航 */
.calendar-nav {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--text-color);
}

.arrow {
  font-size: 16px;
  color: var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.arrow:hover {
  color: #409eff;
  transform: scale(1.1);
}

/* 星期 */
.calendar-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary-color);
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

/* 日期 */
.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  text-align: center;
}

.calendar-day {
  width: 32px;
  height: 32px;
  line-height: 32px;
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-main-color);
  margin: 0 auto;
  transition: all 0.2s ease;
  cursor: pointer;
}

.calendar-day:hover:not(.other-month) {
  background-color: rgba(64, 160, 255, 0.2);
  color: #ffffff;
}

.calendar-day.other-month {
  color: var(--text-prompt-color);
  cursor: default;
}

.calendar-day.today {
  background: linear-gradient(135deg, #409eff, #003ae5);
  color: #fff;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

/* 选中的日期 */
.calendar-day.selected {
  background-color: #f38600 !important;
  color: #ffffff !important;
  font-weight: bold;
}

/* 回到今天按钮 hover */
.today-btn:hover {
  color: var(--primary-color) !important;
}

/* 站点信息统计 */
.site-stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.site-stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
  min-width: 0;
  padding: 8px 10px;
  border-radius: 8px;
  background: var(--card-secound-bg);
}

.site-stat-label {
  font-size: 12px;
  color: var(--text-secondary-color);
  white-space: nowrap;
}

.site-stat-value {
  max-width: 100%;
  overflow: hidden;
  color: var(--text-data-prompt-color);
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
  text-overflow: ellipsis;
}

.site-stat-uptime {
  display: flex;
  grid-column: 1 / -1;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: var(--card-secound-bg);
}

.site-stat-uptime-value {
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 600;
}

main {
  flex: 1;
  min-width: 0;
  overflow-x: hidden;
}

/* 手机端侧栏不做吸顶，避免遮挡右侧内容 */
@media (max-width: 930px) {
  .sidebar {
    position: static !important;
    max-height: none !important;
    overflow: visible !important;
  }
}
</style>

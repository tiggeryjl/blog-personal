<script setup>
import { useRoute, useRouter } from 'vue-router';
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import WelcomeBanner from '@/components/WelcomeBanner.vue';
import { ElMessage } from 'element-plus';
import { getPersonalInfoApi } from '@/api/auth.js';
import { getCategoryListApi } from '@/api/category.js';
import { getTagListApi } from '@/api/tag.js';
import { getArticleCalendarApi } from '@/api/article.js';
import { subscribeRssApi } from '@/api/rss.js';
import { useSiteStatisticsStore } from '@/store/siteStatistics';

const route = useRoute();
const router = useRouter();

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
    const result = await getPersonalInfoApi();
    if (result.code === 200) {
      userProfile.value = result.data;
    }
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

// 邮箱订阅：点击订阅按钮弹出对话框填邮箱，提交后写入订阅表，新文章发布时后端定时发信
const subscribeDialogVisible = ref(false);
const subscribing = ref(false);
const subscribeForm = reactive({
  email: '',
  nickname: '',
});
const EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

const openSubscribeDialog = () => {
  subscribeDialogVisible.value = true;
};

const closeSubscribeDialog = () => {
  // 提交中不允许关闭，避免用户以为没提交上
  if (subscribing.value) return;
  subscribeDialogVisible.value = false;
};

// el-dialog 的关闭拦截（右上角叉、Esc、点遮罩都会走这里）：提交中不关闭
const handleSubscribeDialogClose = (done) => {
  if (subscribing.value) return;
  done();
};

const submitSubscribe = async () => {
  if (subscribing.value) return;

  const email = subscribeForm.email.trim();
  if (!email) {
    ElMessage.warning('请先填写邮箱');
    return;
  }
  if (!EMAIL_PATTERN.test(email)) {
    ElMessage.warning('邮箱格式不正确');
    return;
  }

  subscribing.value = true;
  try {
    const result = await subscribeRssApi({
      email,
      nickname: subscribeForm.nickname.trim() || undefined,
    });
    if (result.code === 200) {
      ElMessage.success('订阅成功');
      subscribeForm.email = '';
      subscribeForm.nickname = '';
      subscribeDialogVisible.value = false;
    } else {
      ElMessage.warning(result.msg || '订阅失败，请稍后再试');
    }
  } catch (error) {
    ElMessage.error('订阅失败，请稍后再试');
  } finally {
    subscribing.value = false;
  }
};

const categoryList = ref([]);

const getCategoryList = async () => {
  try {
    const result = await getCategoryListApi();
    if (result.code === 200) {
      categoryList.value = result.data;
    }
  } catch (error) {
    console.error('获取分类列表失败:', error);
  }
};

// 当前选中的分类（来自文章页 URL 上的 categoryId），用于侧边栏高亮
const activeCategoryId = computed(() => {
  const id = route.query.categoryId;
  if (id == null || id === '') return null;
  return String(Array.isArray(id) ? id[0] : id);
});

// 点击分类标签：跳转到文章列表页并携带分类筛选条件
const goCategory = (category) => {
  router.push({ path: '/article', query: { categoryId: category.id } });
};

// 当前选中的标签（来自文章页 URL 上的 tag），用于侧边栏高亮
const activeTagName = computed(() => {
  const tag = route.query.tag;
  if (tag == null || tag === '') return null;
  return String(Array.isArray(tag) ? tag[0] : tag);
});

// 点击标签：跳转到文章列表页并携带标签筛选条件
const goTag = (tag) => {
  moreDialogVisible.value = false;
  router.push({ path: '/article', query: { tag: tag.name } });
};

const tagList = ref([]);

const getTagList = async () => {
  try {
    const result = await getTagListApi();
    if (result.code === 200) {
      tagList.value = result.data;
    }
    await rescanTagOverflow();
  } catch (error) {
    console.error('获取标签列表失败:', error);
  }
};

// 站点信息由全局WebSocket实时更新
const siteStats = useSiteStatisticsStore();
const runningTimeText = computed(() => {
  if (!siteStats.loaded) return '--';
  const days = Math.floor(siteStats.runningSeconds / 86400);
  const hours = Math.floor((siteStats.runningSeconds % 86400) / 3600);
  const minutes = Math.floor((siteStats.runningSeconds % 3600) / 60);
  const seconds = siteStats.runningSeconds % 60;
  return `${days}天 ${hours}时 ${minutes}分 ${seconds}秒`;
});
let runningTimer = null;

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

// 当前选中的日期（来自文章页 URL 上的 date 参数，格式 yyyy-MM-dd）
const selectedDateKey = computed(() => {
  const value = route.query.date;
  if (value == null || value === '') return '';
  return String(Array.isArray(value) ? value[0] : value);
});

// 带日期筛选条件进入时，日历直接停在对应的月份
const initialDateKey = (() => {
  const matched = /^(\d{4})-(\d{2})/.exec(selectedDateKey.value);
  if (!matched) return null;
  const month = Number(matched[2]);
  if (month < 1 || month > 12) return null;
  return { year: Number(matched[1]), month };
})();

const currentYear = ref(initialDateKey ? initialDateKey.year : today.getFullYear());
const currentMonth = ref(initialDateKey ? initialDateKey.month : today.getMonth() + 1);

// 当前月份里每天的文章数量，形如 { '2026-10-01': 2 }
const calendarArticleCounts = ref({});

const pad2 = (value) => String(value).padStart(2, '0');
const formatDateKey = (year, month, day) => `${year}-${pad2(month)}-${pad2(day)}`;

// 拉取当前月份每天的文章数量，用于在日期下方打标识
const loadCalendarCounts = async () => {
  try {
    const result = await getArticleCalendarApi({ year: currentYear.value, month: currentMonth.value });
    if (result.code === 200 && Array.isArray(result.data)) {
      const countMap = {};
      result.data.forEach((item) => {
        if (item.date) countMap[String(item.date)] = Number(item.count) || 0;
      });
      calendarArticleCounts.value = countMap;
    } else {
      calendarArticleCounts.value = {};
    }
  } catch (error) {
    console.error('获取日历文章数量失败:', error);
    calendarArticleCounts.value = {};
  }
};

// 点击日期：跳转到文章页，并按当天的发布时间筛选文章
const selectDay = (day) => {
  if (day.isOtherMonth || !day.key) return;
  router.push({ path: '/article', query: { date: day.key } });
};

// 回到今天：日历切回当前月份，同时清空日期筛选（分类、标签等其它筛选条件保留）
const goToday = () => {
  currentYear.value = today.getFullYear();
  currentMonth.value = today.getMonth() + 1;
  if (route.query.date != null) {
    const query = { ...route.query };
    delete query.date;
    router.push({ path: route.path, query });
  }
};

// 上一个月
const prevMonth = () => {
  currentMonth.value--;
  if (currentMonth.value < 1) {
    currentMonth.value = 12;
    currentYear.value--;
  }
};

// 下一个月
const nextMonth = () => {
  currentMonth.value++;
  if (currentMonth.value > 12) {
    currentMonth.value = 1;
    currentYear.value++;
  }
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
    const key = formatDateKey(year, currentMonth.value, i);

    days.push({
      date: i,
      key: key,
      // 当天发布的文章数量，用于在日期下方显示标识
      count: calendarArticleCounts.value[key] || 0,
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

// 切换月份后重新统计该月每天的文章数量
watch([currentYear, currentMonth], () => {
  loadCalendarCounts();
});

// 日期筛选条件变化时（例如从别处带着日期进来），日历自动跳到对应月份
watch(selectedDateKey, (value) => {
  const matched = /^(\d{4})-(\d{2})/.exec(value);
  if (!matched) return;
  const year = Number(matched[1]);
  const month = Number(matched[2]);
  if (month < 1 || month > 12) return;
  if (currentYear.value !== year || currentMonth.value !== month) {
    currentYear.value = year;
    currentMonth.value = month;
  }
});

const show = computed(() => {
  return route.path === '/' || route.path.startsWith('/index');
});

onMounted(async () => {
  getPersonalInfo();
  getCategoryList();
  getTagList();
  loadCalendarCounts();
  await rescanTagOverflow();
  window.addEventListener('resize', rescanTagOverflow);
  if (document.fonts?.ready) {
    document.fonts.ready.then(() => rescanTagOverflow());
  }
  runningTimer = setInterval(() => siteStats.tickRunningTime(), 1000);
});

onUnmounted(() => {
  window.removeEventListener('resize', rescanTagOverflow);
  if (runningTimer) clearInterval(runningTimer);
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
            <span>{{ siteStats.articleCount }}</span>
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
          <a href="javascript:void(0)" title="订阅新文章" @click="openSubscribeDialog">
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

        <!--分类排列 -->
        <div v-if="viewMode === 'category'" class="category-mode-list">
          <div
            v-for="category in categoryList"
            :key="category.id"
            class="category-row"
            :class="{ active: String(category.id) === activeCategoryId }"
            @click="goCategory(category)"
          >
            <span class="category-row-dot"></span>
            <span class="category-row-name">{{ category.name }}</span>
            <span class="category-row-count">{{ category.articleCount }} 篇</span>
          </div>
        </div>

        <!-- 标签排列 -->
        <template v-else>
          <div ref="tagAreaRef" class="tag-area">
            <span
              v-for="tag in displayTags"
              :key="tag.id"
              class="tag-chip"
              :class="{ active: tag.name === activeTagName }"
              :style="tagColorStyle(tag.name)"
              @click="goTag(tag)"
            >
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
            <el-icon class="arrow today-btn" title="回到今天" @click="goToday" style="margin-left: 6px">
              <RefreshRight />
            </el-icon>
            <el-icon class="arrow" title="上个月" @click="prevMonth">
              <ArrowLeft />
            </el-icon>
            <span class="calendar-month-label">{{ currentYear }}年{{ currentMonth }}月</span>
            <el-icon class="arrow" title="下个月" @click="nextMonth">
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
              class="calendar-day"
              :class="{
                'other-month': day.isOtherMonth,
                today: day.isToday,
                selected: !!day.key && day.key === selectedDateKey,
                'has-article': day.count > 0,
              }"
              :title="day.count > 0 ? `${day.key} 发布了 ${day.count} 篇文章` : ''"
              @click="selectDay(day)"
            >
              <span class="calendar-day-num">{{ day.date }}</span>
              <!-- 有文章的日期在下方显示标识 -->
              <span v-if="day.count > 0" class="calendar-day-dot"></span>
            </span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main>
      <router-view />
    </main>

    <!-- 右侧侧边栏 -->
    <aside class="sidebar right-sidebar" aria-label="站点信息">
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
            <span class="site-stat-value">{{ siteStats.onlineVisitor ?? 0 }}</span>
          </div>
          <div class="site-stat-item">
            <span class="site-stat-label">今日浏览</span>
            <span class="site-stat-value">{{ siteStats.todayView ?? 0 }}</span>
          </div>
          <div class="site-stat-item">
            <span class="site-stat-label">总浏览量</span>
            <span class="site-stat-value">{{ siteStats.totalView ?? 0 }}</span>
          </div>
          <div class="site-stat-item">
            <span class="site-stat-label">总访问量</span>
            <span class="site-stat-value">{{ siteStats.totalVisitor ?? 0 }}</span>
          </div>
          <div class="site-stat-uptime">
            <span class="site-stat-label">运行时长</span>
            <span class="site-stat-uptime-value">{{ runningTimeText ?? 0 }}</span>
          </div>
        </div>
      </div>
    </aside>
  </div>

  <el-dialog
    v-model="moreDialogVisible"
    class="more-tags-dialog"
    :title="`更多标签（${hiddenTagCount}）`"
    width="400px"
    align-center
  >
    <div class="dialog-tag-cloud">
      <span
        v-for="tag in remainingTags"
        :key="tag.id"
        class="tag-chip"
        :class="{ active: tag.name === activeTagName }"
        :style="tagColorStyle(tag.name)"
        @click="goTag(tag)"
      >
        {{ tag.name }}
      </span>
      <span v-if="remainingTags.length === 0" class="dialog-tag-empty">暂无更多标签</span>
    </div>
  </el-dialog>

  <!-- 邮箱订阅对话框：el-dialog + 自定义外观，观感与之前的手写弹窗保持一致 -->
  <el-dialog
    v-model="subscribeDialogVisible"
    class="subscribe-dialog"
    width="420px"
    align-center
    :before-close="handleSubscribeDialogClose"
  >
    <template #header>
      <h3 class="subscribe-dialog-title">RSS订阅</h3>
    </template>

    <div class="subscribe-dialog-field">
      <input
        id="subscribe-nickname"
        v-model.trim="subscribeForm.nickname"
        class="subscribe-input"
        type="text"
        maxlength="30"
        placeholder="昵称"
        @keyup.enter="submitSubscribe"
      />
    </div>
    <div class="subscribe-dialog-field">
      <input
        id="subscribe-email"
        v-model.trim="subscribeForm.email"
        class="subscribe-input"
        type="email"
        placeholder="邮箱 *"
        @keyup.enter="submitSubscribe"
      />
    </div>

    <template #footer>
      <div class="subscribe-dialog-footer">
        <button type="button" class="subscribe-cancel-btn" @click="closeSubscribeDialog">取消</button>
        <button type="button" class="subscribe-btn" :disabled="subscribing" @click="submitSubscribe">
          {{ subscribing ? '提交中…' : '确认订阅' }}
        </button>
      </div>
    </template>
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

/* 两侧栏共享宽度、吸顶和独立滚动行为 */
.sidebar {
  width: 330px;
  /* 固定宽度 */
  flex-shrink: 0;
  align-self: flex-start;
  position: sticky;
  top: 90px;
  max-height: calc(100vh - 110px);
  max-height: calc(100dvh - 110px);
  overflow-y: auto;
  overscroll-behavior-y: contain;
  scrollbar-gutter: stable;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar:hover {
  scrollbar-color: var(--text-prompt-color) transparent;
}

.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar::-webkit-scrollbar-thumb {
  border-radius: 6px;
  background: transparent;
}

.sidebar:hover::-webkit-scrollbar-thumb {
  background: var(--text-prompt-color);
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
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
  /* border-bottom: 1px solid var(--border-color); */
}

.category-row:hover {
  background-color: var(--card-bg-hover);
}

.category-row.active .category-row-name {
  color: var(--primary-color);
  font-weight: 600;
}

.category-row.active .category-row-dot {
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.18);
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

.tag-chip.active {
  font-weight: 600;
  box-shadow: 0 0 0 2px currentColor;
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
  gap: 6px;
  font-size: 14px;
  color: var(--text-color);
}

.calendar-month-label {
  min-width: 76px;
  text-align: center;
  font-weight: 500;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 15px;
  color: var(--border-color);
  cursor: pointer;
  transition: all 0.2s ease;
}

.arrow:hover {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.12);
  transform: scale(1.1);
}

/* 星期 */
.calendar-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary-color);
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

/* 周末用主题色区分，方便快速定位 */
.calendar-week span:first-child,
.calendar-week span:last-child {
  color: #409eff;
}

/* 日期 */
.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 3px;
  text-align: center;
}

.calendar-day {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 34px;
  aspect-ratio: 1 / 1;
  margin: 0 auto;
  border-radius: 8px;
  font-size: 13.5px;
  color: var(--text-main-color);
  user-select: none;
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
  cursor: pointer;
}

.calendar-day:hover:not(.other-month) {
  background-color: rgba(64, 158, 255, 0.14);
  color: #409eff;
  transform: translateY(-1px);
}

.calendar-day.other-month {
  color: var(--text-prompt-color);
  cursor: default;
  opacity: 0.6;
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
  background-image: none !important;
  color: #ffffff !important;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(243, 134, 0, 0.45);
}

/* 有文章的日期：数字下方的小圆点标识 */
.calendar-day.has-article:not(.today):not(.selected) {
  color: var(--text-color);
  font-weight: 600;
}

.calendar-day-dot {
  position: absolute;
  left: 50%;
  bottom: 3px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #ff4800;
  transform: translateX(-50%);
  transition: background-color 0.2s ease;
}

.calendar-day.today .calendar-day-dot,
.calendar-day.selected .calendar-day-dot {
  background-color: #ffffff;
}

/* 回到今天按钮 hover */
.today-btn:hover {
  color: var(--primary-color) !important;
}

.subscribe-form {
  display: flex;
  gap: 8px;
}

.subscribe-input {
  flex: 1;
  min-width: 0;
  padding: 11px 14px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: var(--card-secound-bg);
  color: var(--text-color);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease;
}

.subscribe-input:focus {
  border-color: var(--primary-color);
}

.subscribe-input::placeholder {
  color: var(--text-prompt-color);
}

.subscribe-btn {
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

.subscribe-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.35);
}

.subscribe-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 订阅对话框（el-dialog + 自定义外观） */
.subscribe-dialog-title {
  margin: 0;
  color: var(--text-color);
  font-size: 16px;
  font-weight: 600;
  line-height: 1;
}

.subscribe-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.subscribe-dialog-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}

.subscribe-cancel-btn {
  padding: 7px 18px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: transparent;
  color: var(--text-secondary-color);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.subscribe-cancel-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
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

.site-stat-article {
  grid-column: 1 / -1;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
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

/* 三栏在平板和小屏笔记本上保持可读宽度 */
@media (min-width: 931px) and (max-width: 1180px) {
  .blog-layout {
    gap: 12px !important;
    padding: 15px !important;
  }

  .sidebar {
    width: clamp(220px, 22vw, 260px) !important;
    max-width: 260px !important;
  }

  .profile-card,
  .category-card {
    padding: 14px;
  }

  .search-card {
    padding: 12px;
  }

  .profile-card .stats {
    gap: 18px;
  }

  .category-nav-head,
  .calendar-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .calendar-header {
    margin-bottom: 12px;
  }

  .calendar-nav {
    width: 100%;
    justify-content: space-between;
    gap: 4px;
    font-size: 12px;
  }

  .calendar-days {
    gap: 2px;
  }

  .calendar-day {
    max-width: 26px;
    font-size: 12px;
  }

  .calendar-day-dot {
    bottom: 2px;
    width: 3px;
    height: 3px;
  }

  .calendar-month-label {
    min-width: 0;
  }

  .right-sidebar .site-stat-grid {
    grid-template-columns: 1fr;
  }

  .right-sidebar .site-stat-uptime {
    grid-column: 1;
    flex-direction: column;
    align-items: flex-start;
  }
}

/* 手机端侧栏不做吸顶，避免遮挡右侧内容 */
@media (max-width: 930px) {
  .sidebar {
    position: static !important;
    max-height: none !important;
    overflow: visible !important;
    overscroll-behavior-y: auto;
    scrollbar-gutter: auto;
  }
}
</style>

<!--
  el-dialog 会渲染到组件作用域之外，scoped 的 :deep() 拿不到它的内部节点，
  所以订阅弹窗的外观覆写单独放在这个非 scoped 块里，用 .subscribe-dialog 限定作用范围。
-->
<style>
.el-dialog.subscribe-dialog {
  max-width: calc(100vw - 40px);
  padding: 20px 22px 18px;
  border-radius: 12px;
  background-color: var(--card-secound-bg);
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
}

.subscribe-dialog .el-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 0 16px;
  padding: 0;
}

.subscribe-dialog .el-dialog__headerbtn {
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

.subscribe-dialog .el-dialog__headerbtn:hover {
  background-color: var(--card-bg-hover);
  color: var(--hover-color);
}

.subscribe-dialog .el-dialog__close {
  color: inherit;
  font-size: 14px;
}

.subscribe-dialog .el-dialog__body {
  padding: 0;
  color: var(--text-color);
}

.subscribe-dialog .el-dialog__footer {
  padding: 0;
  margin-top: 18px;
}
</style>

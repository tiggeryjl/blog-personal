<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import WelcomeBanner from '@/components/WelcomeBanner.vue'

const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth() + 1)

// 上一个月
const prevMonth = () => {
  currentMonth.value--
  if (currentMonth.value < 1) {
    currentMonth.value = 12
    currentYear.value--
  }
}

// 下一个月
const nextMonth = () => {
  currentMonth.value++
  if (currentMonth.value > 12) {
    currentMonth.value = 1
    currentYear.value++
  }
}

// 生成日历数据
const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value - 1
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const daysInPrevMonth = new Date(year, month, 0).getDate()

  const days = []

  // 上月
  for (let i = 0; i < firstDay; i++) {
    days.push({
      date: daysInPrevMonth - firstDay + i + 1,
      isOtherMonth: true,
      isToday: false,
    })
  }

  // 当月
  for (let i = 1; i <= daysInMonth; i++) {
    const isToday =
      i === today.getDate() &&
      month === today.getMonth() &&
      year === today.getFullYear()

    days.push({
      date: i,
      isOtherMonth: false,
      isToday: isToday,
    })
  }

  // 下月补全
  const remain = 42 - days.length
  for (let i = 1; i <= remain; i++) {
    days.push({
      date: i,
      isOtherMonth: true,
      isToday: false,
    })
  }

  return days
})

const router = useRouter()
const route = useRoute()

const show = computed(() => {
  return route.path === '/' || route.path.startsWith('/index')
})

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
        <img src="https://picsum.photos/64/64" alt="头像" class="avatar" />
        <h3 class="nickname">你的昵称</h3>
        <p class="bio">这是我的个人简介，记录生活与技术思考 ✨</p>
        <div class="stats">
          <div class="stats-item">
            <span>文章</span>
            <span>10</span>
          </div>
          <div class="stats-item">
            <span>分类</span>
            <span>10</span>
          </div>
        </div>
        <div class="links">
          <a href="javascript:void(0)">github</a>
          <a href="javascript:void(0)">email</a>
          <a href="javascript:void(0)">QQ</a>
        </div>
      </div>

      <!-- 2. 分类/公告区域 -->
      <div class="category-card">
        <h4>📌 分类导航</h4>
        <ul class="category-list">
          <li>前端开发</li>
          <li>后端技术</li>
          <li>生活随笔</li>
          <li>项目分享</li>
        </ul>
      </div>

      <!-- 美化版日历组件 -->
      <div class="category-card">
        <div class="calendar-header">
          <h4>📅 日历</h4>
          <div class="calendar-nav">
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
            <span v-for="(day, index) in calendarDays" :key="index" :class="{
              'calendar-day': true,
              'other-month': day.isOtherMonth,
              'today': day.isToday,
            }">
              {{ day.date }}
            </span>
          </div>
        </div>
      </div>

    </aside>

    <!-- 右侧主内容区 -->
    <main>
      <router-view />
    </main>

  </div>
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
  margin-bottom: 12px;
  border: 2px solid var(--primary-color);
}

.nickname {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: bold;
  color: var(--text-color);
}

.bio {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary-color);
  line-height: 1.5;
}

/* 文章 | 分类 统计 —— 上下排列核心 */
.profile-card .stats {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin: 6px 0;
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
  color: var(--text-color);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
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

.category-card h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: var(--text-color);
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
  color: var(--text-main-color);
}

.category-list li {
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: color 0.3s;
}

.category-list li:hover {
  color: var(--hover-color);
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

main {
  flex: 1;
  min-width: 0;
  overflow-x: hidden;
}
</style>
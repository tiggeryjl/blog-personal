<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted, onUnmounted } from 'vue'

const router = useRouter()
const dailyList = ref([
  {
    id: 1,
    userId: 101,
    avatar: 'https://picsum.photos/64/64',
    username: '张三',
    content: '今天天气不错，分享一张随手拍～',
    images: [
      'https://picsum.photos/600/400?random=1',
      'https://picsum.photos/600/400?random=2'
    ],
    createTime: '2025-12-20 15:30',
    like: 23,
    comment: 5,
    isLiked: false
  },
  {
    id: 2,
    userId: 102,
    avatar: 'https://picsum.photos/64/64?random=2',
    username: '李四',
    content: '记录一下今天的学习日常：Vue3 + ElementPlus 开发中',
    images: [],
    createTime: '2025-12-20 14:10',
    like: 12,
    comment: 3,
    isLiked: false
  }
])

// 点赞
const toggleLike = (item) => {
  item.isLiked = !item.isLiked
  item.like += item.isLiked ? 1 : -1
}

// 去评论页
const goComment = (id) => {
  router.push(`/daily/${id}`)
}

</script>

<template>
  <div class="common-article">
    <!-- 🔒 固定区域：标题 + 分割线 + 发布框（不受排版影响） -->
    <div class="fixed-header">
      <h2>最新日常</h2>
      <div class="publish-box" v-if="false">
        <!-- 你的发布输入框代码 -->
      </div>
      <div class="divider"></div>
    </div>

    <div class="daily-list">
      <div class="daily-item" v-for="item in dailyList" :key="item.id">
        <!-- 头像 -->
        <div class="card-header">
          <el-avatar :src="item.avatar" size="42" />
          <div class="info">
            <div class="username">{{ item.username }}</div>
            <div class="time">{{ item.createTime }}</div>
          </div>
        </div>

        <!-- 文字内容 -->
        <div class="card-content">{{ item.content }}</div>

        <!-- 图片 -->
        <div class="card-images" v-if="item.images.length">
          <img v-for="img in item.images" :key="img" :src="img" />
        </div>

        <!-- 点赞 + 评论 -->
        <div class="card-actions">
          <button class="like-btn" :class="{ active: item.isLiked }" @click.stop="toggleLike(item)">
            <font-awesome-icon icon="fa-solid fa-thumbs-up" /> {{ item.like }}
          </button>
          <button class="comment-btn" @click.stop="goComment(item.id)">
            💬 {{ item.comment }}
          </button>
        </div>
      </div>

      <div v-if="dailyList.length === 0" class="empty-data">
        暂无日常数据
      </div>
    </div>
  </div>
</template>

<style scoped>
.common-article {
  flex: 1;
  /* 占满剩余宽度 */
  padding: 5px 12px;
  border-radius: 8px;
  background-color: var(--card-bg);
}


/* 🔒 固定头部区域：永远保持原样 */
.fixed-header {
  margin-bottom: 20px;
}

.fixed-header h2 {
  margin: 0 0 8px;
  color: var(--text-color);
}

.divider {
  height: 1px;
  background: var(--border-color);
  margin-bottom: 20px;
}

.daily-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.daily-item {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 头像 */
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header .info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
}

.time {
  font-size: 12px;
  color: var(--text-secondary-color);
}

/* 文字内容 */
.card-content {
  color: var(--text-main-color);
  font-size: 15px;
  line-height: 1.6;
  word-break: break-all;
}

/* 图片网格 */
.card-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 8px;
}

.card-images img {
  width: 100%;
  height: 90px;
  object-fit: cover;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.2s;
}

.card-images img:hover {
  transform: scale(1.03);
}

/* 操作栏 */
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding-top: 4px;
}

.like-btn,
.comment-btn {
  background: transparent;
  border: none;
  font-size: 13px;
  color: var(--text-secondary-color);
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 6px;
}

.like-btn:hover,
.comment-btn:hover {
  background: var(--card-bg-hover);
}

.like-btn.active {
  color: #ff4d64;
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
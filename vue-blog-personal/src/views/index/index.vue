<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { DArrowRight } from '@element-plus/icons-vue'
import WelcomeBanner from '@/components/WelcomeBanner.vue'

const router = useRouter()

// 公告内容（你自己随便改）
const notice = ref(`【公告】
欢迎来到我的个人博客！
这里会记录技术文章、日常感悟、学习笔记~

后续更新计划：
▫️ 完善文章发布功能
▫️ 新增评论系统
▫️ 优化页面样式
▫️ 增加 dark/light 模式`)

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
        <span>文章列表<el-icon>
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
        <span>日常分享<el-icon>
            <DArrowRight />
          </el-icon></span>
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
  background: rgb(255, 207, 183);
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
</style>
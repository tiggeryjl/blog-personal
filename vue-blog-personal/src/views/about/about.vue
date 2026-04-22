<script setup>
import { ref, onMounted } from 'vue'
const isAnimated = ref(false)

onMounted(() => {
  setTimeout(() => {
    isAnimated.value = true
  }, 100)
})
</script>

<template>
  <div class="about-page">
    <!-- 顶部个人卡片 -->
    <div class="profile-card" :class="{ show: isAnimated }">
      <div class="avatar">
        <img src="https://picsum.photos/64/64" alt="头像" />
      </div>
      <h1>小叶同学</h1>
      <p class="title">Java 后端开发者 | 全栈学习者</p>
      <p class="motto">持续输入，认真输出，慢慢成长</p>
    </div>

    <!-- 内容区域 -->
    <div class="content-wrapper">
      <div class="section" :class="{ show: isAnimated }" style="animation-delay: 0.2s">
        <h2>👋 关于我</h2>
        <p>
          你好，我是一名软件技术专业的学生，主攻 Java 后端开发，同时也在学习前端、数据库、服务器相关知识。
          我享受从困惑到理解、从理论到实践的过程，喜欢把学到的知识整理成文字，方便自己复盘，也希望能帮助到更多人。
        </p>
        <p>
          在学习的路上，我始终相信：<b>输入是学习，输出才是真正的掌握</b>。
          写博客不仅是记录，更是一种思考方式，也是我成长轨迹最真实的见证。
        </p>
      </div>

      <div class="section" :class="{ show: isAnimated }" style="animation-delay: 0.35s">
        <h2>📖 关于博客</h2>
        <p>
          这是我的个人技术博客，主要用于记录学习笔记、项目实战、踩坑总结、技术思考与经验分享。
          我会在这里持续更新自己的学习历程，不追求高产，但力求每一篇内容都真实、有用、可复现。
        </p>
        <p>
          希望这个小小的空间，能成为我技术路上的灯塔，也能成为你学习路上的一点帮助。
        </p>
      </div>

      <div class="section" :class="{ show: isAnimated }" style="animation-delay: 0.5s">
        <h2>💻 技术方向</h2>
        <div class="skills">
          <span>Java</span>
          <span>Spring Boot</span>
          <span>MySQL</span>
          <span>MyBatis</span>
          <span>Vue3</span>
          <span>HTML/CSS</span>
          <span>Linux</span>
          <span>Git</span>
        </div>
      </div>

      <div class="section" :class="{ show: isAnimated }" style="animation-delay: 0.65s">
        <h2>✨ 我的理念</h2>
        <ul class="idea-list">
          <li>代码不仅是功能，更是思考与逻辑的体现</li>
          <li>先理解原理，再熟练使用，最后灵活创新</li>
          <li>保持好奇，持续学习，踏实沉淀，稳步成长</li>
          <li>技术没有捷径，唯有多写、多练、多思考</li>
        </ul>
      </div>

      <div class="section" :class="{ show: isAnimated }" style="animation-delay: 0.8s">
        <h2>📮 联系我</h2>
        <div class="contact">
          <div>GitHub：https://github.com/tiggeryjl</div>
          <div>邮箱：2087691050@qq.com</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 动画：从上往下淡入 */
.profile-card,
.section {
  opacity: 0;
  transform: translateY(-15px);
  transition: all 0.5s ease;
}

.profile-card.show,
.section.show {
  opacity: 1;
  transform: translateY(0);
}

/* 头像微动 */
.avatar:hover {
  transform: scale(1.04);
  transition: transform 0.3s ease;
}

/* 技能标签hover */
.skills span:hover {
  background: var(--primary-color);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(66, 153, 225, 0.2);
  transition: all 0.25s ease;
}

/* 列表hover微动 */
.idea-list li:hover {
  transform: translateX(4px);
  transition: transform 0.2s ease;
}

/* 你原来的全部样式 */
.about-page {
  min-height: calc(100vh - 65px);
  padding: 20px 9%;
}

.container {
  max-width: 900px;
  margin: 0 auto;
}

.profile-card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 40px 20px;
  text-align: center;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 20px;
  border: 2px solid var(--primary-color);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-card h1 {
  font-size: 26px;
  margin: 0 0 8px;
  color: var(--text-color);
}

.title {
  font-size: 16px;
  color: var(--text-main-color);
  margin: 0 0 10px;
}

.motto {
  font-size: 15px;
  color: var(--text-secondary-color);
}

.content-wrapper {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section {
  margin-bottom: 35px;
}

.section h2 {
  font-size: 30px;
  color: var(--text-color);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--border-color);
}

.section p {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-main-color);
  margin-bottom: 12px;
}

.skills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skills span {
  background: var(--card-bg-hover);
  border: 1px solid var(--border-color);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 14px;
  color: var(--text-main-color);
}

.idea-list {
  padding-left: 20px;
  color: var(--text-main-color);
  line-height: 1.8;
}

.contact {
  line-height: 1.8;
  color: var(--text-main-color);
}

@media (max-width: 768px) {

  .profile-card,
  .content-wrapper {
    padding: 25px 18px;
  }

  .section h2 {
    font-size: 18px;
  }
}
</style>
<script setup>
import { ref, onMounted } from 'vue';

const isAnimated = ref(false);

onMounted(() => {
  // 等首帧渲染完成后再触发，顺序淡入的过渡才会真正执行
  setTimeout(() => {
    isAnimated.value = true;
  }, 60);
});
</script>

<template>
  <div class="about-page">
    <div class="about-card">
      <section class="section" :class="{ show: isAnimated }">
        <h3 class="section-title">
          <svg
            viewBox="0 0 24 24"
            width="17"
            height="17"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          关于我
        </h3>
        <div class="section-body">
          <p>你好，我是<strong>小叶同学</strong>，一名普通计科专业的博主！</p>
          <p>
            主攻 Java 后端开发，同时也在学习前端、数据库和服务器相关知识。主要兴趣和精力都放在了后端开发与 Web 技术上。
          </p>
          <p>
            我享受从困惑到理解、从理论到实践的过程，喜欢把学到的知识整理成文字，方便自己复盘，也希望能帮助到更多人。
          </p>
          <div class="highlight">输入是学习，输出才是真正的掌握。</div>
        </div>
      </section>

      <section class="section" :class="{ show: isAnimated }">
        <h3 class="section-title">
          <svg
            viewBox="0 0 24 24"
            width="17"
            height="17"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <circle cx="12" cy="12" r="9" />
            <path d="m8.5 12.2 2.4 2.4 4.6-4.8" />
          </svg>
          我的理念
        </h3>
        <ul class="idea-list">
          <li>代码不仅是功能，更是思考与逻辑的体现</li>
          <li>先理解原理，再熟练使用，最后灵活创新</li>
          <li>保持好奇，持续学习，踏实沉淀，稳步成长</li>
          <li>技术没有捷径，唯有多写、多练、多思考</li>
        </ul>
      </section>

      <section class="section" :class="{ show: isAnimated }">
        <h3 class="section-title">
          <svg
            viewBox="0 0 24 24"
            width="17"
            height="17"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
          </svg>
          关于博客
        </h3>
        <div class="section-body">
          <p>这个博客对我而言，远不止一个技术笔记本。</p>
          <p>
            这里主要记录学习笔记、项目实战、踩坑总结和技术思考，也会写下一些日常琐碎与阶段性感悟——记录这些，才是我最初建立这里的根本原因。
          </p>
          <p>
            我过去没有写日记的习惯，以至于许多当时觉得重要的时刻和感受，都在时间里变得模糊不清。于是我想在这里诚实地留下一些痕迹：记录下解决问题时的思路，也记录下某个深夜的触动；分享学到的知识，也分享那些不成熟但真实的思考。
          </p>
          <p>
            我不追求高产，但力求每一篇内容都真实、有用、可复现。也相信未来的某一天回看这些文字，无论当时是为一个技术难点焦头烂额，还是为一件小事感慨，都能让那一刻的我和现在的我重逢。
          </p>
          <p>感谢你的到访。这里没什么特别，只是一个学生记录成长、思考和生活的地方，欢迎常来坐坐。</p>
        </div>
      </section>

      <section class="section" :class="{ show: isAnimated }">
        <h3 class="section-title">
          <svg
            viewBox="0 0 24 24"
            width="17"
            height="17"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <rect x="2" y="4" width="20" height="16" rx="2" />
            <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
          </svg>
          联系我
        </h3>
        <div class="contact-list">
          <!-- <div class="contact-item">
            <span class="contact-label">GitHub</span>
            <a class="contact-link" href="https://github.com/tiggeryjl" target="_blank" rel="noopener">
              https://github.com/tiggeryjl
            </a>
          </div> -->
          <div class="contact-item">
            <span class="contact-label">QQ</span>
            <span class="contact-value">2087691050</span>
          </div>
          <div class="contact-item">
            <span class="contact-label">邮箱</span>
            <a class="contact-link" href="mailto:2087691050@qq.com">2087691050@qq.com</a>
          </div>
          <div class="contact-item">
            <span class="contact-label">留言板</span>
            <router-link class="contact-link" to="/feedback">去留言板看看 →</router-link>
          </div>
        </div>
      </section>

      <section class="section" :class="{ show: isAnimated }">
        <h3 class="section-title">
          <svg
            viewBox="0 0 24 24"
            width="17"
            height="17"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <rect x="2" y="3" width="20" height="14" rx="2" />
            <path d="M8 21h8" />
            <path d="M12 17v4" />
          </svg>
          关于这个网站
        </h3>
        <div class="section-body">
          <!-- TODO: 按你的实际情况改这一段描述 -->
          <p>
            本站是一个前后端分离的个人博客，采用简约风格，支持默认 / 深色 / 浅色三套主题，文章、日常、友链、留言、RSS
            订阅等功能均为自己开发实现。
          </p>
          <!-- TODO: 按你的实际情况增删下面的技术栈标签 -->
          <div class="tech-row">
            <span class="tech-label">前端</span>
            <div class="skills">
              <span>Vue3</span>
              <span>Vite</span>
              <span>Element Plus</span>
              <span>Pinia</span>
              <span>Vue Router</span>
            </div>
          </div>
          <div class="tech-row">
            <span class="tech-label">后端</span>
            <div class="skills">
              <span>Java</span>
              <span>Spring Boot</span>
              <span>MyBatis</span>
              <span>MySQL</span>
            </div>
          </div>
          <div class="tech-row">
            <span class="tech-label">部署</span>
            <div class="skills">
              <span>Linux</span>
              <span>Nginx</span>
              <span>Docker</span>
              <span>Git</span>
            </div>
          </div>
          <p class="github-hint">
            如果喜欢这样的风格，或者对某些功能感兴趣，欢迎去
            <a class="inline-link" href="https://github.com/tiggeryjl" target="_blank" rel="noopener">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                <path
                  d="M12 .3a12 12 0 0 0-3.8 23.38c.6.12.83-.26.83-.57L9 21.07c-3.34.72-4.04-1.61-4.04-1.61-.55-1.39-1.34-1.76-1.34-1.76-1.08-.74.08-.73.08-.73 1.2.08 1.84 1.24 1.84 1.24 1.07 1.83 2.81 1.3 3.5 1 .1-.78.42-1.3.76-1.6-2.67-.3-5.47-1.33-5.47-5.93 0-1.31.47-2.38 1.24-3.22-.13-.3-.54-1.52.12-3.18 0 0 1-.33 3.3 1.23a11.5 11.5 0 0 1 6.02 0c2.28-1.56 3.29-1.23 3.29-1.23.66 1.66.25 2.88.12 3.18a4.65 4.65 0 0 1 1.23 3.22c0 4.61-2.81 5.63-5.48 5.93.43.37.81 1.1.81 2.22l-.01 3.29c0 .31.22.69.83.57A12 12 0 0 0 12 .3"
                />
              </svg>
              GitHub
            </a>
            看看源码。
          </p>
        </div>
      </section>

      <section class="section copyright-section" :class="{ show: isAnimated }">
        <h4 class="copyright-title">版权声明</h4>
        <p class="copyright-text">
          本博客所有原创文章采用
          <strong>CC BY-NC-SA 4.0</strong> 协议进行许可。转载请注明出处，禁止商业使用，演绎作品需以相同方式共享。
        </p>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* ========== 入场动画：从上往下顺序淡入 ========== */
.page-header,
.section {
  opacity: 0;
  transform: translateY(-15px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.page-header.show,
.section.show {
  opacity: 1;
  transform: translateY(0);
}

/* 顺序延迟（transition-delay 才会真正生效） */
.page-header {
  transition-delay: 0s;
}

.about-card > .section:nth-child(1) {
  transition-delay: 0.15s;
}

.about-card > .section:nth-child(2) {
  transition-delay: 0.25s;
}

.about-card > .section:nth-child(3) {
  transition-delay: 0.35s;
}

.about-card > .section:nth-child(4) {
  transition-delay: 0.45s;
}

.about-card > .section:nth-child(5) {
  transition-delay: 0.55s;
}

.about-card > .section:nth-child(6) {
  transition-delay: 0.65s;
}

/* 尊重"减少动态效果"的系统设置 */
@media (prefers-reduced-motion: reduce) {
  .page-header,
  .section {
    opacity: 1;
    transform: none;
    transition: none;
  }
}

/* ========== 页面容器 ========== */
/* 现在渲染在带侧边栏的三栏布局里，外层 .blog-layout 已经有内边距，
   这里再加 9% 的横向 padding 会在侧边栏和卡片之间多出一大片空白 */
.about-page {
  min-height: calc(100vh - 65px);
  padding: 0;
}

/* ========== 页面标题 ========== */
.page-header {
  text-align: center;
  margin-bottom: 22px;
}

.page-header h2 {
  font-size: 30px;
  font-weight: 600;
  color: var(--text-color);
  letter-spacing: 2px;
}

.page-header p {
  font-size: 16px;
  color: var(--text-main-color);
  margin-top: 8px;
}

.divider {
  height: 1px;
  background: var(--border-color);
  margin: 14px 0 0;
  opacity: 0.35;
}

/* ========== 内容卡片 ========== */
.about-card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 0 28px;
  backdrop-filter: blur(6px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.section {
  padding: 26px 0;
}

/* 区块之间的浅色分隔线 */
.section:not(:first-child)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--border-color);
  opacity: 0.16;
}

/* ========== 区块标题 ========== */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 14px;
}

.section-title svg {
  flex-shrink: 0;
  color: var(--primary-color);
}

/* ========== 正文 ========== */
.section-body {
  font-size: 15px;
  line-height: 1.9;
  color: var(--text-main-color);
}

.section-body p {
  margin-bottom: 12px;
}

.section-body p:last-child {
  margin-bottom: 0;
}

.section-body strong {
  font-weight: 600;
  color: var(--text-color);
}

.highlight {
  margin-top: 16px;
  padding: 12px 16px;
  border-left: 3px solid var(--primary-color);
  border-radius: 0 10px 10px 0;
  background: var(--summary-bg);
  font-size: 15px;
  font-weight: 500;
  line-height: 1.8;
  color: var(--text-color);
}

/* ========== 理念列表 ========== */
.idea-list {
  display: grid;
  gap: 10px;
  list-style: none;
}

.idea-list li {
  position: relative;
  padding-left: 22px;
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-main-color);
  transition: transform 0.2s ease, color 0.2s ease;
}

.idea-list li::before {
  content: '✦';
  position: absolute;
  left: 0;
  top: 0;
  color: var(--primary-color);
  font-size: 13px;
}

.idea-list li:hover {
  transform: translateX(4px);
  color: var(--text-color);
}

/* ========== 联系方式 ========== */
.contact-list {
  display: grid;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
}

.contact-label {
  flex-shrink: 0;
  width: 62px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary-color);
}

.contact-value {
  color: var(--text-color);
  word-break: break-all;
}

.contact-link {
  color: var(--text-color);
  font-weight: 500;
  text-decoration: none;
  border-bottom: 1px dashed var(--border-color);
  word-break: break-all;
  transition: color 0.2s ease, border-color 0.2s ease;
}

.contact-link:hover {
  color: var(--primary-color);
  border-bottom-style: solid;
  border-bottom-color: var(--primary-color);
}

/* ========== 技术栈 ========== */
.tech-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.tech-row + .tech-row {
  margin-top: 12px;
}

.tech-label {
  flex-shrink: 0;
  width: 44px;
  padding-top: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary-color);
  letter-spacing: 0.5px;
}

.skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skills span {
  padding: 5px 13px;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  background: var(--card-bg-hover);
  font-size: 13px;
  color: var(--text-main-color);
  transition: all 0.25s ease;
}

.skills span:hover {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(66, 153, 225, 0.2);
}

.github-hint {
  margin-top: 16px !important;
}

.inline-link {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: var(--text-color);
  font-weight: 600;
  text-decoration: none;
  border-bottom: 1px solid var(--border-color);
  transition: color 0.2s ease, border-color 0.2s ease;
}

.inline-link:hover {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

/* ========== 版权声明 ========== */
.copyright-section {
  margin: 0 -28px;
  padding: 20px 28px;
  background: var(--card-secound-bg);
}

.copyright-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary-color);
  margin-bottom: 6px;
}

.copyright-text {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary-color);
}

.copyright-text strong {
  font-weight: 600;
  color: var(--text-main-color);
}

/* ========== 响应式 ========== */
@media (max-width: 992px) {
  .page-header h2 {
    font-size: 27px;
  }
}

@media (max-width: 768px) {
  .about-card {
    padding: 0 16px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .page-header p {
    font-size: 14px;
  }

  .section {
    padding: 20px 0;
  }

  .section-title {
    font-size: 16px;
  }

  .section-body {
    font-size: 14px;
  }

  .contact-label {
    width: 56px;
  }

  .tech-row {
    flex-direction: column;
    gap: 8px;
  }

  .tech-label {
    width: auto;
    padding-top: 0;
  }

  .copyright-section {
    margin: 0 -16px;
    padding: 18px 16px;
  }
}
</style>

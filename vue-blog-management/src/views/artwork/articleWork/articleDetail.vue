<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import { getArticleDetailApi } from '@/api/article.js';
import { getTagOptionsApi } from '@/api/tag.js';
import { getStatusText, getStatusType } from '@/constants/articleConstants';

const route = useRoute();
const router = useRouter();
const article = ref({});
const loading = ref(false);
const tagOptions = ref([]);

// 封面作为详情页背景（无封面时使用默认渐变背景）
const heroStyle = computed(() => {
  if (article.value.cover) {
    return { backgroundImage: `url(${article.value.cover})` };
  }
  return {};
});

// 标签 ID 转名称（详情接口返回的是标签 ID 列表）
const getTagName = (tagId) => {
  const found = tagOptions.value.find((item) => String(item.value) === String(tagId));
  return found ? found.label : String(tagId);
};

const displayTags = () => {
  const tags = article.value.tags || [];
  return tags.map((tag) => getTagName(tag));
};

const loadArticle = async (id) => {
  loading.value = true;
  try {
    const result = await getArticleDetailApi(id);
    if (result.code === 200) {
      article.value = result.data || {};
    } else {
      ElMessage.error(result.msg || '文章加载失败');
      router.back();
    }
  } catch (error) {
    ElMessage.error('文章加载异常，请重试');
    router.back();
  } finally {
    loading.value = false;
  }
};

// 加载标签选项用于名称映射，失败时标签展示原始值，不阻断页面
const loadTagOptions = async () => {
  try {
    const result = await getTagOptionsApi();
    if (result.code === 200) {
      tagOptions.value = result.data || [];
    }
  } catch (error) {
    // 忽略：标签选项加载失败不影响文章详情展示
  }
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  const id = route.query.id || route.params.id;
  if (!id) {
    ElMessage.error('缺少文章ID，无法查看详情');
    router.back();
    return;
  }
  loadTagOptions();
  loadArticle(id);
});
</script>

<template>
  <div class="article-detail-page">
    <!-- 顶部操作栏 -->
    <div class="detail-toolbar">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="toolbar-title">文章详情</span>
    </div>

    <!-- 加载骨架屏 -->
    <el-skeleton v-if="loading" animated :rows="8" class="detail-skeleton" />

    <template v-else>
      <!-- 文章不存在 -->
      <el-empty v-if="!article.id" description="文章不存在或已被删除">
        <el-button type="primary" :icon="ArrowLeft" @click="goBack">返回</el-button>
      </el-empty>

      <!-- 文章详情卡片 -->
      <el-card v-else class="detail-card" shadow="hover">
        <!-- 封面背景 + 标题区 -->
        <div class="article-hero" :style="heroStyle">
          <div class="hero-overlay"></div>
          <div class="hero-content">
            <h1 class="article-title">{{ article.title || '未命名文章' }}</h1>

            <!-- 状态/置顶/热门标签：位于标题与用户信息之间 -->
            <div class="header-badges">
              <el-tag v-if="article.status !== undefined" :type="getStatusType(article.status)" effect="dark">
                {{ getStatusText(article.status) }}
              </el-tag>
              <el-tag v-if="article.isTop === 1" type="warning" effect="plain">置顶</el-tag>
              <el-tag v-if="article.isHot === 1" type="danger" effect="plain">热门</el-tag>
            </div>

            <!-- 作者信息 -->
            <div class="author-row">
              <el-avatar :size="36" :src="article.userAvatar || ''">
                {{ (article.userNickname || '无').slice(0, 1) }}
              </el-avatar>
              <span class="author-name">{{ article.userNickname || '匿名作者' }}</span>
              <span class="author-divider">·</span>
              <span class="author-time">
                {{ article.publishTime || article.timedPublishTime || article.createTime || '未发布' }}
              </span>
            </div>
          </div>
        </div>

        <div class="detail-body">
          <!-- 摘要 -->
          <div v-if="article.summary" class="summary-box">
            <span class="summary-label">摘要</span>
            <p class="summary-text">{{ article.summary }}</p>
          </div>

          <!-- 元信息 -->
          <el-descriptions :column="3" border class="meta-descriptions">
            <el-descriptions-item label="分类">{{ article.category || '未分类' }}</el-descriptions-item>
            <el-descriptions-item label="阅读">{{ article.viewNum ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="点赞">{{ article.likeNum ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="评论">{{ article.commentNum ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ article.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ article.updateTime || '-' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 标签 -->
          <div v-if="displayTags().length" class="tag-box">
            <span class="tag-label">标签</span>
            <el-tag v-for="(tag, idx) in displayTags()" :key="`tag-${idx}`" type="primary" effect="light" size="small">
              {{ tag }}
            </el-tag>
          </div>

          <!-- 正文 -->
          <div class="content-card">
            <div v-if="article.content" class="article-content" v-html="article.content"></div>
            <el-empty v-else description="暂无文章内容" :image-size="80" />
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.article-detail-page {
  max-width: 1000px;
  margin: 0 auto;
}

.detail-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
}

.toolbar-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 1px;
}

.detail-skeleton {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.detail-card {
  border-radius: 12px;
  overflow: hidden;
}

.detail-card :deep(.el-card__body) {
  padding: 0;
}

/* 封面背景区 */
.article-hero {
  position: relative;
  min-height: 320px;
  padding: 48px 40px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  text-align: center;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: #0b3d91;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.15) 0%, rgba(0, 0, 0, 0.6) 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  width: 100%;
}

.article-title {
  font-size: 30px;
  font-weight: 700;
  color: #fff;
  line-height: 1.4;
  margin: 0 0 18px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
  word-break: break-word;
}

.header-badges {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.author-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.author-row :deep(.el-avatar) {
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.author-name {
  color: #fff;
  font-weight: 600;
}

.author-divider {
  color: rgba(255, 255, 255, 0.5);
}

.detail-body {
  padding: 28px;
}

.summary-box {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 8px;
  padding: 14px 18px;
  margin-bottom: 24px;
}

.summary-label {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

.summary-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

.meta-descriptions {
  margin-bottom: 24px;
}

.tag-box {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.tag-label {
  font-size: 14px;
  color: #606266;
}

.content-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 24px;
}

.article-content {
  font-size: 15px;
  line-height: 1.9;
  color: #333;
  word-break: break-word;
}

/* wangEditor 输出的正文排版（v-html 注入内容需使用 :deep） */
.article-content :deep(p) {
  margin: 0 0 1em;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4),
.article-content :deep(h5),
.article-content :deep(h6) {
  margin: 1.4em 0 0.8em;
  line-height: 1.5;
  color: #1d2129;
  font-weight: 700;
}

.article-content :deep(h1) {
  font-size: 26px;
}

.article-content :deep(h2) {
  font-size: 22px;
}

.article-content :deep(h3) {
  font-size: 19px;
}

.article-content :deep(h4) {
  font-size: 17px;
}

.article-content :deep(h5),
.article-content :deep(h6) {
  font-size: 16px;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 16px auto;
  border-radius: 8px;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  padding-left: 2em;
  margin: 0 0 1em;
}

.article-content :deep(li) {
  margin: 6px 0;
}

.article-content :deep(blockquote) {
  border-left: 4px solid #c0c4cc;
  background: #f7f8fa;
  color: #555;
  padding: 12px 16px;
  margin: 0 0 1em;
  border-radius: 0 8px 8px 0;
}

.article-content :deep(pre) {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 0 0 1em;
  font-size: 13px;
  line-height: 1.6;
}

.article-content :deep(code) {
  background: #f6f8fa;
  color: #c7254e;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}

.article-content :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
}

.article-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 0 0 1em;
}

.article-content :deep(th),
.article-content :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
}

.article-content :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

.article-content :deep(a) {
  color: #409eff;
}

.article-content :deep(hr) {
  border: none;
  border-top: 1px solid #ebeef5;
  margin: 24px 0;
}

.article-content :deep(strong) {
  font-weight: 700;
}

@media (max-width: 768px) {
  .article-hero {
    min-height: 240px;
    padding: 32px 20px;
  }

  .article-title {
    font-size: 22px;
  }

  .detail-body {
    padding: 18px;
  }
}
</style>

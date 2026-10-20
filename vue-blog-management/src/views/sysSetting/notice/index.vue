<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getHomeNoticeConfigApi, saveHomeNoticeApi } from '@/api/home.js';

const loading = ref(false);
const saving = ref(false);
const lastUpdated = ref('');

const form = reactive({
  enabled: false,
  title: '',
  content: '',
});

// 读取当前公告配置
const loadNotice = async () => {
  loading.value = true;
  try {
    const result = await getHomeNoticeConfigApi();
    if (result.code === 200 && result.data) {
      form.enabled = !!result.data.enabled;
      form.title = result.data.title || '';
      form.content = result.data.content || '';
      lastUpdated.value = result.data.updatedAt || '';
    }
  } finally {
    loading.value = false;
  }
};

// 保存公告
const saveNotice = async () => {
  if (form.enabled && !form.content.trim()) {
    ElMessage.warning('启用公告时必须填写公告内容');
    return;
  }
  saving.value = true;
  try {
    const result = await saveHomeNoticeApi({
      enabled: form.enabled,
      title: form.title,
      content: form.content,
    });
    if (result.code === 200) {
      ElMessage.success('公告已保存');
      await loadNotice();
    } else {
      ElMessage.error(result.msg);
    }
  } finally {
    saving.value = false;
  }
};

onMounted(loadNotice);
</script>

<template>
  <div class="notice-config-page" v-loading="loading">
    <el-card shadow="never" class="notice-card">
      <template #header>
        <div class="card-head">
          <div class="card-head-left">
            <span class="card-title">首页公告</span>
            <el-tag :type="form.enabled ? 'success' : 'info'" size="small" effect="light" round>
              {{ form.enabled ? '已启用' : '未启用' }}
            </el-tag>
          </div>
          <span v-if="lastUpdated" class="update-time">最后保存：{{ lastUpdated }}</span>
        </div>
      </template>

      <div class="notice-layout">
        <!-- 编辑区 -->
        <section class="editor">
          <el-form label-position="top" class="notice-form">
            <el-form-item>
              <div class="switch-row">
                <div>
                  <div class="switch-label">启用公告</div>
                  <div class="switch-desc">启用后，用户打开首页时会弹出这则公告</div>
                </div>
                <el-switch v-model="form.enabled" />
              </div>
            </el-form-item>

            <el-form-item label="公告标题">
              <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="不填则显示「网站公告」" />
            </el-form-item>

            <el-form-item label="公告内容">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="9"
                maxlength="2000"
                show-word-limit
                placeholder="支持换行，用户端会按原样显示"
              />
            </el-form-item>

            <div class="form-actions">
              <el-button type="primary" :loading="saving" @click="saveNotice">保存公告</el-button>
              <el-button @click="loadNotice">重新加载</el-button>
            </div>
          </el-form>
        </section>

        <!-- 预览区 -->
        <aside class="preview-pane">
          <div class="preview-label">用户端弹窗预览</div>
          <div class="preview-dialog" :class="{ disabled: !form.enabled }">
            <div class="preview-header">
              <span class="preview-title">{{ form.title || '网站公告' }}</span>
              <span class="preview-close">×</span>
            </div>
            <div class="preview-content">{{ form.content || '公告内容会显示在这里…' }}</div>
            <div class="preview-footer">
              <span class="preview-btn">我知道了</span>
            </div>
          </div>
          <p class="preview-tip">关闭后用户端不再弹出；再次修改并保存后，用户会重新看到公告。</p>
        </aside>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.notice-config-page {
  width: 100%;
}

.notice-card :deep(.el-card__header) {
  padding: 18px 22px;
  border-bottom: 1px solid #ebeef5;
}

.notice-card :deep(.el-card__body) {
  padding: 24px 22px 26px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-head-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.update-time {
  font-size: 13px;
  color: #909399;
}

/* 左编辑、右预览 */
.notice-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 1fr);
  gap: 36px;
}

.editor {
  min-width: 0;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  width: 100%;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f7f8fa;
}

.switch-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.switch-desc {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: #909399;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}

/* 预览区 */
.preview-pane {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.preview-label {
  margin-bottom: 12px;
  font-size: 13px;
  color: #909399;
}

.preview-dialog {
  padding: 20px 22px 18px;
  border: 1px solid #e8ebf0;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff, #f2f5fb);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  transition: opacity 0.2s ease;
}

.preview-dialog.disabled {
  opacity: 0.55;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.preview-close {
  color: #c0c4cc;
  font-size: 18px;
  line-height: 1;
}

.preview-content {
  max-height: 220px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.9;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
}

.preview-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.preview-btn {
  padding: 7px 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409eff, #003ae5);
  color: #fff;
  font-size: 13px;
}

.preview-tip {
  margin: 14px 0 0;
  font-size: 12px;
  line-height: 1.7;
  color: #a8abb2;
}

@media (max-width: 1100px) {
  .notice-layout {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .preview-pane {
    max-width: 520px;
  }
}
</style>

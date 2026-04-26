<script setup>
import { ref } from 'vue'
import EditorView from '@/components/MyEditor.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({ title: '', content: '' })

const submit = () => {
  console.log('标题：' + form.value.title)
  console.log('内容：' + form.value.content) // 获取 HTML 内容
  // 发送给后端...
}

const clearContent = () => {
  form.value = { title: '', content: '' }
}
const cancel = () => {
  router.go(-1)
}
</script>

<template>
  <div class="article-publish-page">
    <div class="publish-container">
      <h1>文章编辑</h1>
      <!-- 标题 -->
      <div class="form-item">
        <label class="form-label">文章标题</label>
        <input v-model="form.title" type="text" class="title-input" placeholder="请输入文章标题" />
      </div>

      <!-- 内容 -->
      <div class="form-item">
        <label class="form-label">文章内容</label>
        <EditorView v-model="form.content" />
      </div>

      <!-- 按钮组 -->
      <div class="btn-group">
        <button class="btn btn-submit" @click="submit">发布文章</button>
        <button class="btn btn-reset" @click="clearContent">清空内容</button>
        <button class="btn btn-cancel" @click="cancel">取消发布</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.article-publish-page {
  min-height: calc(100vh - 65px);
  width: 84%;
  margin: 0 auto;
  padding: 30px 0;
  display: flex;
  justify-content: center;
}

/* 发布卡片 */
.publish-container {
  width: 100%;
  max-width: 1100px;
  background: var(--card-bg);
  border-radius: 16px;
  padding: 36px 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.publish-container h1 {
  margin: -20px auto;
  color: var(--text-color);
  text-align: center;
}

/* 表单项 */
.form-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: -10px;
}

/* 标签 */
.form-label {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
  letter-spacing: 0.5px;
  padding-left: 14px;
}

.form-label::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 4px;
  height: 20px;
  background: var(--primary-color);
  border-radius: 2px;
}

/* 标题输入框 */
.title-input {
  height: 50px;
  padding: 0 16px;
  border-radius: 10px;
  border: 2px solid var(--border-color);
  background: #f3f4f6;
  color: #000;
  font-size: 16px;
  transition: all 0.2s ease;
}

.title-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

/* 按钮组 */
.btn-group {
  display: flex;
  justify-content: flex-end;
  justify-content: center;
  gap: 14px;
}

/* 按钮通用 */
.btn {
  height: 40px;
  padding: 0 24px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

/* 取消/清空按钮 */
.btn-reset {
  background: #374151;
  color: #d1d5db;
}

.btn-reset:hover {
  color: #1a1c1f;
  background: #738194;
}

.btn-cancel {
  background: linear-gradient(180deg, #ff0000 0%, #ff4040 100%);
  color: #fff;
}

.btn-cancel:hover {
  background: linear-gradient(180deg, #c21111 0%, #d44040 100%);
}



/* 提交按钮 */
.btn-submit {
  background: #2563eb;
  color: #fff;
}

.btn-submit:hover {
  background: #113086;
}

.btn-submit:active {
  transform: scale(0.98);
}
</style>
<script setup>
import { ref, onMounted } from 'vue'
import EditorView from '@/components/MyEditor.vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElSelect, ElOption, ElUpload } from 'element-plus'
import { getCategoryOptionsApi } from '@/api/category.js'
import { getTagOptionsApi } from '@/api/tag.js'
import { addArticleApi } from '@/api/article.js'
import { useUserStore } from '@/stores/userloginstatus'

const router = useRouter()

const articleForm = ref({
  id: '',
  title: '',
  content: '',
  categoryId: '',
  tags: [],
  cover: ''
})

//单独为上传图片功能设置请求头token，从pinia里拿
const userStore = useUserStore()

// 图片上传成功后触发
const handleCoverSuccess = (response) => {
  articleForm.value.cover = response.data
}
// 文件上传之前触发
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('只支持上传图片')
    return false
  } else if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('只能上传10M以内图片')
    return false
  }
  return true
}

// 分类列表
const categoryOptions = ref([])
// 获取分类下拉选项
const getCategoryOptions = async () => {
  try {
    const result = await getCategoryOptionsApi()
    if (result.code === 200) {
      categoryOptions.value = result.data
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败!');
  }
}

// const tagOptions = ref([
//   { label: 'Vue', value: 'Vue' },
//   { label: '前端', value: '前端' },
//   { label: 'JavaScript', value: 'JavaScript' },
//   { label: 'React', value: 'React' },
//   { label: '生活', value: '生活' },
//   { label: '随想', value: '随想' }
// ])
//标签列表
const tagOptions = ref([])
const getTagOptions = async () => {
  try {
    const result = await getTagOptionsApi()
    if (result.code === 200) {
      tagOptions.value = result.data
    }
  } catch (error) {
    ElMessage.error('获取标签列表失败!');
  }
}

const submit = async () => {
  try {
    const result = await addArticleApi(articleForm)
    if (result.code === 200) {
      ElMessage.success('草稿保存成功!');
      router.go(-1)
    }
  } catch (error) {
    ElMessage.error('保存失败,接口异常!');
  }
}

const clearContent = () => {
  ElMessage.success(`已清空`)
  articleForm.value = { title: '', content: '', categoryId: '', tags: [], cover: '' }
}
const cancel = () => {
  router.go(-1)
}
const publish = () => {
  ElMessage.success(`保存并发布`)
}

onMounted(() => {
  getCategoryOptions()
  getTagOptions()
})
</script>

<template>
  <div class="publish-container">
    <h1>文章编辑</h1>

    <!-- 封面上传 -->
    <div class="form-item">
      <label class="form-label">文章封面</label>
      <el-upload class="cover-upload" action="/api/upload/upload" :show-file-list="false"
        :on-success="handleCoverSuccess" :before-upload="beforeAvatarUpload"
        :headers="{ Authorization: `Bearer ${userStore.token}` }">
        <div v-if="articleForm.cover" class="cover-preview">
          <img :src="articleForm.cover" alt="封面预览">
        </div>
        <div v-else class="upload-placeholder">
          <span class="plus-icon">+</span>
          <span>添加封面</span>
        </div>
      </el-upload>
    </div>

    <!-- 标题 -->
    <div class="form-item">
      <label class="form-label">文章标题</label>
      <input v-model="articleForm.title" type="text" class="title-input" placeholder="请输入文章标题" />
    </div>

    <div class="form-row">
      <!-- 分类 -->
      <div class="form-item">
        <label class="form-label">文章分类</label>
        <el-select v-model="articleForm.categoryId" placeholder="请选择分类" clearable>
          <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <!-- 标签 -->
      <div class="form-item">
        <label class="form-label">文章标签</label>
        <el-select v-model="articleForm.tags" multiple placeholder="请选择文章标签" clearable>
          <el-option v-for="tag in tagOptions" :key="tag.value" :label="tag.label" :value="tag.value" />
        </el-select>
      </div>
    </div>

    <!-- 内容 -->
    <div class="form-item">
      <label class="form-label">文章内容</label>
      <EditorView v-model="articleForm.content" />
    </div>

    <!-- 按钮组 -->
    <div class="btn-group">
      <button class="btn btn-publish" @click="publish">保存发布</button>
      <button class="btn btn-reset" @click="clearContent">清空内容</button>
      <button class="btn btn-cancel" @click="cancel">取消编辑</button>
      <button class="btn btn-submit" @click="submit">保存草稿</button>
    </div>
  </div>
</template>

<style scoped>
.publish-container {
  width: 100%;
  max-width: 1100px;
  border-radius: 16px;
  padding: 36px 40px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.publish-container h1 {
  margin: -20px auto;
  color: #000;
  text-align: center;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: -10px;
}

.form-label {
  font-size: 16px;
  font-weight: 500;
  color: #000;
  letter-spacing: 0.5px;
  padding-left: 14px;
  position: relative;
}

.form-label::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 4px;
  height: 20px;
  background: #3b82f6;
  border-radius: 2px;
}

/* 真正修改 el-select 输入框高度、宽度、样式 */
:deep(.el-select) {
  width: 100% !important;
}

:deep(.el-select__wrapper) {
  min-height: 52px !important;
  /* 真正的输入框高度 */
  border-radius: 10px !important;
  background: #f3f4f6 !important;
  border: 2px solid #9ec9ff !important;
  box-shadow: none !important;
  padding: 0 15px !important;
}

/* 封面上传 */
.cover-upload {
  width: 100%;
}

.plus-icon {
  font-size: 60px !important;
  /* 超级大 */
  line-height: 1;
  font-weight: 200;
  color: #999;
}

.upload-placeholder {
  height: 180px;
  width: 200px;
  background: #f3f4f6;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: #666;
  gap: 8px;
  cursor: pointer;
}

.cover-preview img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 10px;
}

/* 分类下拉 */
.category-select {
  height: 50px;
  padding: 0 16px;
  border-radius: 10px;
  border: 2px solid #ffffff;
  background: #f3f4f6;
  color: #000;
  font-size: 16px;
  transition: all 0.2s ease;
}

.category-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.title-input {
  height: 50px;
  padding: 0 16px;
  border-radius: 10px;
  border: 2px solid #ffffff;
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

/* 分类+标签 同行 */
.form-row {
  display: flex;
  gap: 20px;
}

.form-row .form-item {
  flex: 1;
}

.btn-group {
  display: flex;
  justify-content: center;
  gap: 14px;
}

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

.btn-reset {
  background: #374151;
  color: #d1d5db;
}

.btn-reset:hover {
  background: #738194;
}

.btn-cancel {
  background: linear-gradient(180deg, #ff0000 0%, #ff4040 100%);
  color: #fff;
}

.btn-cancel:hover {
  background: linear-gradient(180deg, #c21111 0%, #d44040 100%);
}

.btn-publish {
  background: #f8dd0e;
}

.btn-publish:hover {
  background: #d4b52b;
}

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
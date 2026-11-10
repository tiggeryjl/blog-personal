<script setup>
import '@wangeditor/editor/dist/css/style.css';
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { ElMessage } from 'element-plus';
import { uploadApi } from '@/api/upload';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
});
const emit = defineEmits(['update:modelValue']);

// 编辑器实例，必须用 shallowRef，重要！
const editorRef = shallowRef();

const valueHtml = ref(props.modelValue || '');

watch(
  () => props.modelValue,
  (value) => {
    const nextValue = value || '';
    if (nextValue !== valueHtml.value) {
      valueHtml.value = nextValue;
    }
  }
);

watch(valueHtml, (value) => {
  emit('update:modelValue', value);
});

const mode = 'default';

const toolbarConfig = {};

const uploadEditorMedia = async (file, insertFn, mediaType) => {
  const formData = new FormData();
  formData.append('file', file);
  try {
    const result = await uploadApi(formData);
    if (result?.code !== 200 || !result.data) {
      throw new Error(result?.msg || `${mediaType}上传失败`);
    }
    const url = result.data;
    if (mediaType === '图片') {
      insertFn(url, file.name, '');
    } else {
      insertFn(url, '');
    }
  } catch (error) {
    const message =
      error?.response?.data?.msg ||
      error?.message ||
      `${mediaType}上传失败，请稍后重试`;
    ElMessage.error(message);
  }
};

// ========== 重点：配置上传图片、视频 ==========
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    // 图片上传配置
    uploadImage: {
      allowedFileTypes: ['image/*'],
      maxFileSize: 10 * 1024 * 1024, // 10MB 图片限制
      customUpload(file, insertFn) {
        return uploadEditorMedia(file, insertFn, '图片');
      },
    },

    // 视频上传配置
    uploadVideo: {
      allowedFileTypes: ['video/*'],
      maxFileSize: 50 * 1024 * 1024, // 视频50MB
      customUpload(file, insertFn) {
        return uploadEditorMedia(file, insertFn, '视频');
      },
    },
  },
};

// 组件销毁时，也及时销毁编辑器，重要！
onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor) editor.destroy();
});

// 编辑器回调函数
const handleCreated = (editor) => {
  editorRef.value = editor; // 记录 editor 实例，重要！
};
const handleChange = (editor) => {};
const handleDestroyed = (editor) => {};
const handleFocus = (editor) => {};
const handleBlur = (editor) => {};
const customAlert = (info, type = 'warning') => {
  const messageType = ['success', 'warning', 'info', 'error'].includes(type) ? type : 'warning';
  ElMessage({
    message: info,
    type: messageType,
    showClose: true,
    grouping: true,
  });
};
const customPaste = (editor, event, callback) => {
  callback(true);
};
</script>

<template>
  <div class="editor-container">
    <Toolbar
      class="editor-toolbar"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      :mode="mode"
      style="border-bottom: 1px solid #ccc"
    />
    <Editor
      class="editor-content"
      :defaultConfig="editorConfig"
      :mode="mode"
      v-model="valueHtml"
      style="height: 600px; overflow-y: hidden"
      @onCreated="handleCreated"
      @onChange="handleChange"
      @onDestroyed="handleDestroyed"
      @onFocus="handleFocus"
      @onBlur="handleBlur"
      @customAlert="customAlert"
      @customPaste="customPaste"
    />
  </div>
</template>

<style scoped>
.editor-container {
  max-width: 100%;
  margin: 0 auto;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  background-color: #fff;
}

.editor-toolbar {
  border-bottom: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.editor-content {
  min-height: 450px;
  height: auto;
  overflow-y: auto;
}

/* 鼠标悬停/聚焦时的边框高亮 */
.editor-container:hover,
.editor-container:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  transition: all 0.2s ease;
}

.editor-container :deep(.w-e-text-container .w-e-text) {
  font-size: 20px !important;
  line-height: 1.7 !important;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
}

.editor-container :deep(.w-e-text-container) {
  font-size: 20px;
}

:global(.w-e-full-screen-container) {
  z-index: 9999 !important;
  /* 比你的导航栏 z-index 更高 */
  top: 0 !important;
  left: 0 !important;
}
</style>

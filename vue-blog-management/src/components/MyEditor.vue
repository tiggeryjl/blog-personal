<script setup>
import '@wangeditor/editor/dist/css/style.css';
import { onBeforeUnmount, ref, shallowRef, onMounted, computed } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { useUserStore } from '@/stores/userloginstatus'

const userStore = useUserStore()

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})
const emit = defineEmits(['update:modelValue'])

// 编辑器实例，必须用 shallowRef，重要！
const editorRef = shallowRef();

const valueHtml = computed({
  get() { return props.modelValue },
  set(val) { emit('update:modelValue', val) }
})

const mode = 'default';

const toolbarConfig = {};
// ========== 重点：配置上传图片、视频 ==========
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    // 图片上传配置
    uploadImage: {
      server: '/api/upload/upload', // 和页面封面上传接口保持一致
      fieldName: 'file', // 后端接收文件参数名，和el-upload统一
      // 请求头，函数形式实时读取最新token
      headers: {
        Authorization: computed(() => `Bearer ${userStore.token}`).value
      },
      maxFileSize: 10 * 1024 * 1024, // 10MB 图片限制
      // 上传成功回调，把图片插入编辑器
      customInsert(res, insertFn) {
        // res.data 是后端返回的图片URL
        insertFn(res.data)
      },
      // 上传失败提示
      onFailed(file, res) {
        alert(`图片上传失败：${res.msg || '服务器异常'}`)
      }
    },

    // 视频上传配置
    uploadVideo: {
      server: '/api/upload/upload',
      fieldName: 'file',
      headers: {
        Authorization: computed(() => `Bearer ${userStore.token}`).value
      },
      maxFileSize: 50 * 1024 * 1024, // 视频50MB
      customInsert(res, insertFn) {
        const playUrl = res.data + '?response-content-disposition=inline'
        console.log('后端返回视频地址：', playUrl)
        insertFn(playUrl)
      },
      onFailed(file, res) {
        alert(`视频上传失败：${res.msg || '服务器异常'}`)
      }
    }
  }
};

// 组件销毁时，也及时销毁编辑器，重要！
onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor) editor.destroy();;
});

// 编辑器回调函数
const handleCreated = (editor) => {
  editorRef.value = editor; // 记录 editor 实例，重要！
};
const handleChange = (editor) => { };
const handleDestroyed = (editor) => { };
const handleFocus = (editor) => { };
const handleBlur = (editor) => { };
const customAlert = (info, type) => {
  alert(`【自定义提示】${type} - ${info}`);
};
const customPaste = (editor, event, callback) => {
  callback(true);
};
</script>

<template>
  <div class="editor-container">
    <Toolbar class="editor-toolbar" :editor="editorRef" :defaultConfig="toolbarConfig" :mode="mode"
      style="border-bottom: 1px solid #ccc" />
    <Editor class="editor-content" :defaultConfig="editorConfig" :mode="mode" v-model="valueHtml"
      style="height: 600px; overflow-y: hidden " @onCreated="handleCreated" @onChange="handleChange"
      @onDestroyed="handleDestroyed" @onFocus="handleFocus" @onBlur="handleBlur" @customAlert="customAlert"
      @customPaste="customPaste" />
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
<script setup>
import { ref, watch, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import MyPagination from '@/components/MyPagination.vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Plus,
  Search,
  Refresh,
  View,
  Edit,
  Delete,
  Upload,
  Hide,
  Position,
  Timer,
  Close,
  FolderOpened,
  Check,
  FolderRemove,
  ZoomOut,
  ZoomIn,
} from '@element-plus/icons-vue';
import { getStatusText, getStatusType, getStatusOptions } from '@/constants/articleConstants';
import { getCategoryOptionsApi } from '@/api/category.js';
import { getTagOptionsApi } from '@/api/tag.js';
import {
  getArticleListApi,
  setTimedApi,
  cancelTimedApi,
  logicDeleteArticleApi,
  updateArticleStatusApi,
  updateArticleTopApi,
} from '@/api/article.js';
import { getAiChatApi } from '@/api/AIChat.js';
import { ARTICLE_STATUS } from '@/constants/articleConstants';

const router = useRouter();
const statusOptions = getStatusOptions();

// 查询条件
const queryForm = reactive({
  title: '',
  categoryId: '',
  tag: '',
  isTop: '',
  status: '',
  createTime: [],
  begin: '',
  end: '',
});

//侦听queryForm中的createTime属性
watch(
  () => queryForm.createTime,
  (newVal, oldVal) => {
    if (newVal.length == 2) {
      queryForm.begin = newVal[0];
      queryForm.end = newVal[1];
    } else {
      queryForm.begin = '';
      queryForm.end = '';
    }
  }
);

// 文章列表
const articleList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const getArticleList = async () => {
  const params = {
    ...queryForm,
    page: currentPage.value,
    pageSize: pageSize.value,
  };
  delete params.createTime;

  try {
    const result = await getArticleListApi(params);
    if (result.code == 200) {
      articleList.value = result.data.rows;
      total.value = result.data.total;
    } else {
      ElMessage.error(result.msg || '获取用户列表失败');
      articleList.value = [];
      total.value = 0;
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  }
};

// 重置查询
const resetQuery = () => {
  queryForm.title = '';
  queryForm.categoryId = '';
  queryForm.tag = '';
  queryForm.isTop = '';
  queryForm.status = '';
  queryForm.createTime = [];
  queryForm.begin = '';
  queryForm.end = '';
  getArticleList();
};

//新增
const goto = (path) => {
  router.push(path);
};

// 编辑
const editArticle = (row) => {
  router.push({
    path: '/editInput',
    query: { id: row.id, status: row.status },
  });
};

// 删除
const deleteArticle = async (id) => {
  ElMessageBox.confirm('您确认要删除该文章吗?', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const result = await logicDeleteArticleApi(id);
    if (result.code == 200) {
      ElMessage.success('删除成功');
      getArticleList();
    } else {
      ElMessage.error(result.msg);
    }
  });
};

// 批量删除选中的行
const selectedArticles = ref([]);
// 表格选中事件
const handleSelectionChange = (val) => {
  selectedArticles.value = val.map((item) => item.id);
};

// 批量删除
const handleBatchDelete = async () => {
  if (!selectedArticles.value || selectedArticles.value.length <= 0) {
    ElMessage.warning('请先勾选至少一篇文章');
    return;
  }

  ElMessageBox.confirm('您确认要删除选中的所有文章吗?', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    if (selectedArticles.value && selectedArticles.value.length > 0) {
      const result = await logicDeleteArticleApi(selectedArticles.value);
      if (result.code == 200) {
        ElMessage.success('批量删除成功');
        getArticleList();
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      ElMessage.error('您未选择任何记录');
    }
  });
};

const previewDialogVisible = ref(false);
const previewRow = ref({});
const previewFullScreen = ref(false);
// 查看
const viewArticle = (row) => {
  previewRow.value = { ...row };
  previewFullScreen.value = false;
  previewDialogVisible.value = true;
};

const openPreviewNewTab = () => {
  if (!previewRow.value || !previewRow.value.id) {
    ElMessage.warning('请先打开文章预览后再使用新标签页预览');
    return;
  }

  const frontUrl = `/articleDetail?id=${encodeURIComponent(previewRow.value.id)}`;
  window.open(frontUrl, '_blank');
};

const buildPreviewHtml = (htmlContent) => {
  if (!htmlContent) {
    return `<div style="padding:60px;text-align:center;color:#999;font-size:16px;">暂无文章内容</div>`;
  }
  return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
*{margin:0;padding:0;box-sizing:border-box;}
body {
  padding: 40px 50px;
  font-size: 17px;
  line-height: 1.9;
  color: #222;
  background: #ffffff;
  max-width: 960px;
  margin: 0 auto;
}
img {max-width:100%;height:auto;display:block;margin:16px auto;border-radius:6px;}
p {margin: 1.2em 0;}
h1 {font-size:28px;margin:24px 0 12px;}
h2 {font-size:24px;margin:22px 0 10px;border-left:5px solid #409eff;padding-left:12px;}
h3 {font-size:20px;margin:20px 0 8px;}
h4,h5,h6 {margin: 16px 0 6px; font-weight: 600;}
ul,ol {padding-left: 28px;margin: 1.2em 0;}
li {margin: 6px 0;}
blockquote {border-left:5px solid #c0c4cc;padding:14px 18px;color:#555;margin:1.5em 0;background:#f7f8fa;border-radius:0 6px 6px 0;}
pre {background:#f4f5f7;padding:18px;border-radius:8px;overflow-x:auto;margin:16px 0;}
code {background:#f4f5f7;padding:2px 6px;border-radius:4px;font-family:Consolas;}
table {border-collapse: collapse;width:100%;margin:16px 0;}
table td,table th {border:1px solid #ddd;padding:10px 14px;}
</style>
</head>
<body>
${htmlContent}
</body>
</html>
`;
};

const timedDialogVisible = ref(false);
const currentTimedId = ref(null);
const timedPublishTime = ref('');
// 禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 86400000;
};
const setTimed = (id) => {
  currentTimedId.value = id;
  timedPublishTime.value = '';
  timedDialogVisible.value = true;
};

// 提交定时设置
const submitSetTimed = async () => {
  if (!timedPublishTime.value) {
    ElMessage.warning('请选择定时时间');
    return;
  }
  try {
    const tempData = {
      id: currentTimedId.value,
      timedPublishTime: timedPublishTime.value,
    };
    const res = await setTimedApi(tempData);
    if (res.code === 200) {
      ElMessage.success('定时发布设置成功');
      timedDialogVisible.value = false;
      getArticleList();
    } else {
      ElMessage.error(res.msg);
    }
  } catch (err) {
    ElMessage.error('设置失败，请重试');
  }
};

// 取消定时
const cancelTimed = (id) => {
  ElMessageBox.confirm('确定要取消定时发布吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '再想想',
    type: 'warning',
  })
    .then(async () => {
      const res = await cancelTimedApi(id);
      if (res.code === 200) {
        ElMessage.success('已取消定时发布');
        getArticleList();
      } else {
        ElMessage.error(res.msg);
      }
    })
    .catch(() => {});
};

/**
 * 统一设置文章状态
 * @param id 文章id
 * @param targetStatus 目标状态
 */
const changeArticleStatus = async (id, targetStatus) => {
  // 二次确认弹窗
  const confirmText =
    {
      [ARTICLE_STATUS.PUBLISHED]: '确认将文章公开发布？',
      [ARTICLE_STATUS.OFFLINE]: '确认下架该文章？',
      [ARTICLE_STATUS.ARCHIVED]: '确认归档该文章？',
      [ARTICLE_STATUS.PRIVATE]: '确认设置为私密文章？',
    }[targetStatus] || '确认修改文章状态？';

  try {
    await ElMessageBox.confirm(confirmText, '操作提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    });
    // 调用后端统一接口
    const result = await updateArticleStatusApi({ id, status: targetStatus });
    if (result.code === 200) {
      ElMessage.success('操作成功');
      getArticleList(); // 刷新列表
    } else {
      ElMessage.error(result.msg);
    }
  } catch (err) {}
};

// 置顶切换
const toggleTop = async (row) => {
  const actionText = row.isTop ? '取消置顶' : '置顶';
  ElMessageBox.confirm(`确认要${actionText}该文章？`, '操作提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const result = await updateArticleTopApi(row.id);
      if (result.code === 200) {
        ElMessage.success(`${actionText}成功`);
        getArticleList();
      } else {
        ElMessage.error(result.msg);
      }
    })
    .catch(() => {});
};

// 图片大图预览
const showImageModal = ref(false);
const previewImageUrl = ref('');
const scale = ref(2);

// 打开预览
const openPreview = (url) => {
  previewImageUrl.value = url;
  showImageModal.value = true;
  scale.value = 2;
};

// 关闭预览
const closeModal = () => {
  showImageModal.value = false;
};

// 缩放 + -
const zoomIn = () => (scale.value += 0.2);
const zoomOut = () => (scale.value = Math.max(0.4, scale.value - 0.2));

// 上一张 / 下一张
const prevImage = () => {};
const nextImage = () => {};

// 分类列表
const categoryOptions = ref([]);
// 获取分类下拉选项
const getCategoryOptions = async () => {
  try {
    const result = await getCategoryOptionsApi();
    if (result.code === 200) {
      categoryOptions.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败!');
  }
};

// 标签列表
const tagOptions = ref([]);
// 获取标签下拉选项
const getTagOptions = async () => {
  try {
    const result = await getTagOptionsApi();
    if (result.code === 200) {
      tagOptions.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取标签列表失败!');
  }
};

// 根据标签 ID 获取名称
const getTagName = (tagId) => {
  if (!tagOptions.value || tagOptions.value.length === 0) return String(tagId);
  const found = tagOptions.value.find((item) => String(item.value) === String(tagId));
  return found ? found.label : String(tagId);
};

onMounted(() => {
  getArticleList();
  getCategoryOptions();
  getTagOptions();
});
</script>

<template>
  <div class="article-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>文章管理</h1>
      <div class="header-buttons">
        <el-button v-perm="'sys:article:add'" type="primary" icon="Plus" @click="goto('/editInput')"
          >发布文章</el-button
        >
        <el-button v-perm="'sys:article:delete'" type="danger" icon="Delete" @click="handleBatchDelete"
          >批量删除</el-button
        >
      </div>
    </div>

    <!-- 查询条件区域 -->
    <el-card class="query-card" shadow="hover">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getArticleList">
        <el-form-item label="文章标题">
          <el-input v-model="queryForm.title" placeholder="请输入标题关键词" style="width: 360px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" style="width: 230px" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="queryForm.tag" placeholder="请输入标签" style="width: 230px" />
        </el-form-item>

        <el-form-item label="置顶">
          <el-select v-model="queryForm.isTop" placeholder="全部" style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="已置顶" value="1" />
            <el-option label="未置顶" value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布状态">
          <el-select v-model="queryForm.status" placeholder="全部" style="width: 160px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布时间">
          <el-date-picker
            v-model="queryForm.createTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="getArticleList">
            <el-icon>
              <Search />
            </el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon>
              <Refresh />
            </el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 文章列表 -->
    <div class="list-card">
      <!-- 表格 -->
      <el-table :data="articleList" style="width: 100%" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="封面" width="140" align="center">
          <template #default="scope">
            <div class="cover-box">
              <img :src="scope.row.cover" class="cover-img" alt="封面" @click="openPreview(scope.row.cover)" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="文章标题" min-width="160" max-width="240" align="center">
          <template #default="{ row }">
            <div class="wrap-title">
              <el-tooltip v-if="row.isHot" content="热门" placement="top">
                <font-awesome-icon icon="fire" class="hot-icon" />
              </el-tooltip>
              {{ row.title }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="category" label="分类" width="70" align="center" />

        <el-table-column label="标签" width="105" align="center">
          <template #default="{ row }">
            <div class="tag-cell">
              <template v-if="row.tags && row.tags.length > 0">
                <el-tooltip placement="top" :show-after="200" popper-class="article-tags-tooltip">
                  <!-- 悬浮框内容：展示全部标签 -->
                  <template #content>
                    <div class="all-tags">
                      <el-tag v-for="(tagId, index) in row.tags" :key="index" size="small" type="primary">
                        {{ getTagName(tagId) }}
                      </el-tag>
                    </div>
                  </template>

                  <!-- 表格内显示：前2个标签 + 多余数量 -->
                  <div class="tag-show-list">
                    <el-tag
                      v-for="(tagId, index) in row.tags.slice(0, 2)"
                      :key="index"
                      size="small"
                      type="primary"
                      effect="light"
                    >
                      {{ getTagName(tagId) }}
                    </el-tag>
                    <span v-if="row.tags.length > 2" class="tag-more"> +{{ row.tags.length - 2 }} </span>
                  </div>
                </el-tooltip>
              </template>
              <span v-else style="color: #aaa; font-size: 13px">——</span>
            </div>
          </template>
        </el-table-column>

        <!-- 置顶列 -->
        <el-table-column label="置顶" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isTop ? 'success' : 'info'">
              {{ scope.row.isTop ? '已置顶' : '未置顶' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="时间" width="200" align="center">
          <template #default="scope">
            <div class="time-group">
              <div>创建：{{ scope.row.createTime || '——' }}</div>
              <div v-if="scope.row.status === 4">定时：{{ scope.row.timedPublishTime || '——' }}</div>
              <div v-else>发布：{{ scope.row.publishTime || '——' }}</div>
              <div>修改：{{ scope.row.updateTime || '——' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="230" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" link :icon="View" @click="viewArticle(scope.row)">预览</el-button>

              <el-button link :icon="Edit" @click="editArticle(scope.row)">修改</el-button>

              <el-button
                v-if="[ARTICLE_STATUS.DRAFT, ARTICLE_STATUS.SCHEDULED].includes(scope.row.status)"
                type="success"
                link
                :icon="Position"
                @click="changeArticleStatus(scope.row.id, ARTICLE_STATUS.PUBLISHED)"
                >立即发布</el-button
              >

              <el-button
                v-if="[ARTICLE_STATUS.PUBLISHED, ARTICLE_STATUS.ARCHIVED].includes(scope.row.status)"
                type="primary"
                link
                :icon="Upload"
                @click="toggleTop(scope.row)"
              >
                {{ scope.row.isTop ? '取消置顶' : '置顶' }}
              </el-button>

              <el-button
                v-if="
                  [ARTICLE_STATUS.PUBLISHED, ARTICLE_STATUS.ARCHIVED, ARTICLE_STATUS.PRIVATE].includes(scope.row.status)
                "
                type="warning"
                link
                :icon="scope.row.status === ARTICLE_STATUS.PRIVATE ? View : Hide"
                @click="
                  changeArticleStatus(
                    scope.row.id,
                    scope.row.status === ARTICLE_STATUS.PRIVATE ? ARTICLE_STATUS.PUBLISHED : ARTICLE_STATUS.PRIVATE
                  )
                "
              >
                {{ scope.row.status === ARTICLE_STATUS.PRIVATE ? '取消私密' : '设为私密' }}
              </el-button>

              <el-button
                v-if="scope.row.status === ARTICLE_STATUS.DRAFT"
                type="warning"
                link
                :icon="Timer"
                @click="setTimed(scope.row.id)"
                >定时发布</el-button
              >

              <el-button
                v-if="scope.row.status === ARTICLE_STATUS.SCHEDULED"
                type="warning"
                link
                :icon="Timer"
                @click="cancelTimed(scope.row.id)"
                >取消定时</el-button
              >

              <el-button
                v-if="[ARTICLE_STATUS.PUBLISHED, ARTICLE_STATUS.OFFLINE].includes(scope.row.status)"
                :type="scope.row.status === ARTICLE_STATUS.PUBLISHED ? 'danger' : 'success'"
                link
                :icon="scope.row.status === ARTICLE_STATUS.PUBLISHED ? Close : Check"
                @click="
                  changeArticleStatus(
                    scope.row.id,
                    scope.row.status === ARTICLE_STATUS.PUBLISHED ? ARTICLE_STATUS.OFFLINE : ARTICLE_STATUS.PUBLISHED
                  )
                "
              >
                {{ scope.row.status === ARTICLE_STATUS.PUBLISHED ? '下架' : '上架' }}
              </el-button>

              <el-button
                v-if="[ARTICLE_STATUS.PUBLISHED, ARTICLE_STATUS.ARCHIVED].includes(scope.row.status)"
                type="info"
                link
                :icon="scope.row.status === ARTICLE_STATUS.ARCHIVED ? FolderRemove : FolderOpened"
                @click="
                  changeArticleStatus(
                    scope.row.id,
                    scope.row.status === ARTICLE_STATUS.ARCHIVED ? ARTICLE_STATUS.PUBLISHED : ARTICLE_STATUS.ARCHIVED
                  )
                "
              >
                {{ scope.row.status === ARTICLE_STATUS.ARCHIVED ? '取消归档' : '归档' }}
              </el-button>

              <el-button type="danger" link :icon="Delete" @click="deleteArticle(scope.row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-box">
        <MyPagination
          :total="total"
          :current-page="currentPage"
          :page-size="pageSize"
          @update:current-page="currentPage = $event"
          @update:page-size="pageSize = $event"
        />
      </div>
    </div>

    <div v-if="showImageModal" class="image-modal" @click.self="closeModal">
      <!-- 关闭按钮 -->
      <div class="img-close" @click="closeModal">✕</div>

      <!-- 上一张 -->
      <!-- <div class="img-prev" @click="prevImage">‹</div> -->

      <!-- 图片 -->
      <img
        :src="previewImageUrl"
        alt="预览"
        class="preview-image"
        :style="{ transform: `scale(${scale})` }"
        @click.stop
        draggable="false"
        user-select="none"
      />

      <!-- 下一张 -->
      <!-- <div class="img-next" @click="nextImage">›</div> -->

      <!-- 缩放按钮 -->
      <div class="img-zoom">
        <div @click="zoomOut">
          <el-icon>
            <ZoomOut />
          </el-icon>
        </div>
        <div @click="zoomIn">
          <el-icon>
            <ZoomIn />
          </el-icon>
        </div>
      </div>
    </div>

    <el-dialog v-model="timedDialogVisible" title="设置定时发布" width="400px">
      <el-form label-width="80px">
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="timedPublishTime"
            type="datetime"
            placeholder="选择发布时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="timedDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSetTimed">确认设置</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="previewDialogVisible"
      width="78%"
      :fullscreen="previewFullScreen"
      append-to-body
      destroy-on-close
    >
      <template #header>
        <div class="preview-header-bar">
          <div class="preview-title">
            <h2>{{ previewRow.title || '无标题文章' }}</h2>
          </div>
          <div class="preview-header-btns">
            <el-button text type="primary" @click="previewFullScreen = !previewFullScreen">
              {{ previewFullScreen ? '退出全屏' : '全屏预览' }}
            </el-button>
            <el-button text type="primary" @click="openPreviewNewTab">新标签打开真实页面</el-button>
          </div>
        </div>
        <div class="preview-meta-info">
          <span
            >头像：<el-image
              v-if="previewRow.userAvatar"
              :src="previewRow.userAvatar"
              fit="cover"
              style="width: 24px; height: 24px; border-radius: 50%; vertical-align: middle; margin: 0 4px"
              preview
            />
            <span v-else>无</span></span
          >
          <span>作者：{{ previewRow.userNickname || '无' }}</span>
          <span>分类：{{ previewRow.category || '无' }}</span>
          <span>发布状态：{{ getStatusText(previewRow.status) }}</span>
          <span>创建时间：{{ previewRow.createTime || '-' }}</span>
        </div>
      </template>

      <iframe
        class="preview-iframe-box"
        :srcdoc="buildPreviewHtml(previewRow.content)"
        sandbox
        frameborder="0"
      ></iframe>

      <template #footer>
        <div class="preview-footer">
          <el-button @click="previewDialogVisible = false">关闭预览</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.article-management-container {
  padding: 0px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-buttons {
  display: flex;
  gap: 12px;
}

.query-card,
.list-card {
  margin-bottom: 20px;
}

.pagination-box {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

.cover-box {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}

.wrap-title {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

.hot-icon {
  color: #ff831e;
}

.time-group {
  font-size: 13px;
  font-weight: 450;
  line-height: 2.3;
  text-align: left;
  color: #393939;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 0px;
  padding: 4px 0;
}

.action-buttons .el-button {
  width: 100%;
  margin: 0;
  justify-content: flex-start;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 图片大图预览样式 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.preview-image {
  max-width: 90%;
  max-height: 90%;
  transition: transform 0.2s;
}

.img-close {
  position: absolute;
  top: 20px;
  right: 30px;
  font-size: 30px;
  color: white;
  cursor: pointer;
}

.img-prev,
.img-next {
  position: absolute;
  top: 50%;
  font-size: 40px;
  color: white;
  cursor: pointer;
  user-select: none;
}

.img-prev {
  left: 30px;
}

.img-next {
  right: 30px;
}

.img-zoom {
  position: absolute;
  bottom: 30px;
  display: flex;
  gap: 20px;
}

.img-zoom div {
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 10px;
}

:deep(.el-table .cell) {
  overflow-x: visible;
}

:deep(.el-table__row) {
  height: auto !important;
}

:deep(.el-table__cell) {
  overflow-x: auto;
  padding: 0 8px;
}
</style>

<style>
/* 不带 scoped，全局有效 */
.article-tags-tooltip {
  background: #ffffff !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: 8px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1) !important;
  padding: 10px 12px !important;
}

.article-tags-tooltip .all-tags {
  max-width: 320px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* 箭头边框色 */
.article-tags-tooltip .el-popper__arrow {
  border-color: #dcdfe6 !important;
}

/* 箭头背景色 */
.article-tags-tooltip .el-popper__arrow::before {
  background: #ffffff !important;
  border-color: #dcdfe6 !important;
}

/* 文章预览整体美化 */
.preview-header-bar {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 16px;
  width: 100%;
}
/* 标题居中核心 */
.preview-title {
  text-align: center;
}
.preview-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
  line-height: 1.4;
}

.preview-header-btns {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

/* 底部元信息 */
.preview-meta-info {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 22px;
  font-size: 13px;
  color: #6c757d;
  justify-content: center;
  flex-wrap: wrap;
}

/* 内容iframe容器美化 */
.preview-iframe-box {
  width: 100%;
  height: 73vh;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  margin-top: 12px;
}

.preview-footer {
  text-align: center;
}
</style>

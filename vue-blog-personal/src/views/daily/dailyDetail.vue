<script setup>
import { DArrowLeft } from '@element-plus/icons-vue'


</script>
<template>
  <div class="common-article-detail">
    <div class="article-detail">
      <button class="button-return" @click="$router.go(-1)"><el-icon>
          <DArrowLeft />
        </el-icon>返回</button>
      <div class="divider">
      </div>
      <h1>日常详情 {{ $route.params.id }}</h1>
      <div class="content">
        这里是日常内容...
      </div>
    </div>
  </div>
</template>

<style scoped>
.common-article-detail {
  color: #ffffff;
  font-size: 50px;
  display: flex;
  gap: 30px;
  min-height: calc(100vh - 65px);
  width: 82%;
  margin: 0 auto;
}

.article-detail {
  background-color: var(--card-bg);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  border-radius: 8px;
  padding: 20px 12px;
}


.button-return {
  display: inline-flex;
  /*按钮内部变成弹性布局 */
  align-items: center;
  /*垂直居中*/
  justify-content: center;
  /*水平居中*/
  gap: 4px;
  /*文字和符号加点间距 */
  width: 80px;
  height: 36px;
  background-color: var(--primary-color);
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
  border-radius: 18px;
  border: 1px solid var(--primary-color);
}

.button-return:hover {
  background-color: #ebb1a2b8;
  color: var(--hover-color);
  border: 1px solid #ebb1a2b8;
}

.divider {
  height: 1px;
  background: var(--border-color);
  margin-bottom: 20px;
}
</style>
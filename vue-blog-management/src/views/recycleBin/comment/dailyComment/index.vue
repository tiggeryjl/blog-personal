<script setup>
import CommentRecycleTable from '@/components/CommentRecycleTable.vue'
import { getRecycleDailyCommentListApi } from '@/api/comment'
</script>

<template>
  <CommentRecycleTable
    page-title="日常评论回收站"
    :list-api="getRecycleDailyCommentListApi"
    recycle-perm="sys:recycleDailyComment:recycle"
    delete-perm="sys:recycleDailyComment:delete"
  />
</template>

<script setup>
import CommentRecycleTable from '@/components/CommentRecycleTable.vue'
import { getRecycleArticleCommentListApi } from '@/api/comment'
</script>

<template>
  <CommentRecycleTable
    page-title="文章评论回收站"
    :list-api="getRecycleArticleCommentListApi"
    recycle-perm="sys:recycleArticleComment:recycle"
    delete-perm="sys:recycleArticleComment:delete"
  />
</template>

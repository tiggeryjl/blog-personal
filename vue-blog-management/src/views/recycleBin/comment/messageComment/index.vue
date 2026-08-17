<script setup>
import CommentRecycleTable from '@/components/CommentRecycleTable.vue'
import { getRecycleMessageCommentListApi } from '@/api/comment'
</script>

<template>
  <CommentRecycleTable
    page-title="留言评论回收站"
    :list-api="getRecycleMessageCommentListApi"
    recycle-perm="sys:recycleMessageComment:recycle"
    delete-perm="sys:recycleMessageComment:delete"
    show-msg-type
  />
</template>

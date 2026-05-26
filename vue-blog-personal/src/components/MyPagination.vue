<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: { type: Number, required: true },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  pageSizes: { type: Array, default: () => [10, 20, 30, 50] }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change'])

const totalPages = computed(() => Math.ceil(props.total / props.pageSize))

const goPage = (page) => {
  if (totalPages.value === 0) return //
  const realPage = Math.max(1, Math.min(page, totalPages.value))
  if (realPage === props.currentPage) return
  emit('update:currentPage', realPage)
  emit('change', realPage)
}

const handleSizeChange = (size) => {
  const newPageSize = Number(size)
  const newTotalPages = Math.ceil(props.total / newPageSize)
  const newCurrentPage = Math.min(props.currentPage, newTotalPages)
  if (newTotalPages === 0) newCurrentPage = 1

  emit('update:pageSize', newPageSize)
  emit('update:currentPage', newCurrentPage)
  emit('change', newCurrentPage)
}

const pageNumbers = computed(() => {
  const total = totalPages.value
  if (total === 0) return [1]

  if (total <= 5) {
    const allPages = []
    for (let i = 1; i <= total; i++) allPages.push(i)
    return allPages
  }

  const current = props.currentPage
  const showSide = 1  // 当前页前后各显示几页，可根据需要提取为 props

  // 生成一个包含所有需要显示的页码（不考虑省略号）的数组
  let pagesSet = new Set()

  pagesSet.add(1)
  pagesSet.add(total)

  // 包含当前页及其前后 showSide 页
  for (let i = current - showSide; i <= current + showSide; i++) {
    if (i >= 1 && i <= total) pagesSet.add(i)
  }

  // 边缘扩展：靠近开头时，额外添加更多连续的前面页码（如1,2,3）
  if (current <= showSide + 1) {
    // 让开头显示 showSide+2 个连续页码（例如1,2,3）
    const extraEnd = Math.min(total, showSide + 2)
    for (let i = 1; i <= extraEnd; i++) {
      pagesSet.add(i)
    }
  }
  // 靠近末尾时，额外添加更多连续的末尾页码
  if (current >= total - showSide) {
    const extraStart = Math.max(1, total - (showSide + 1))
    for (let i = extraStart; i <= total; i++) {
      pagesSet.add(i)
    }
  }

  // 转为数组并排序
  let pages = Array.from(pagesSet).sort((a, b) => a - b)

  // 将连续的页码合并，不连续的地方插入 '...'
  const result = []
  for (let i = 0; i < pages.length; i++) {
    if (i === 0) {
      result.push(pages[i])
      continue
    }
    const prev = pages[i - 1]
    const curr = pages[i]
    if (curr - prev > 1) {
      result.push('...')
    }
    result.push(curr)
  }
  return result
})


</script>

<template>
  <div class="pagination">
    <div class="total">共{{ total }}条记录</div>

    <div class="size-select">
      <span>每页显示</span>
      <select @change="handleSizeChange(+$event.target.value)" :value="pageSize">
        <option v-for="s in pageSizes" :key="s" :value="s">{{ s }} 条</option>
      </select>
    </div>

    <button class="page-btn" :disabled="currentPage === 1 || totalPages === 0" @click="goPage(currentPage - 1)">
      上一页
    </button>

    <template v-for="item in pageNumbers" :key="item">
      <span v-if="item === '...'" class="ellipsis">···</span>
      <button v-else class="page-btn" :class="{ active: item === currentPage }" @click="goPage(item)">
        {{ item }}
      </button>
    </template>

    <button class="page-btn" :disabled="currentPage === totalPages || totalPages === 0"
      @click="goPage(currentPage + 1)">
      下一页
    </button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
  margin: 20px 0;
  color: var(--text-color);
}

.total {
  margin-right: 10px;
  font-size: 14px;
}

.size-select {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-right: 10px;
}

.size-select select {
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-color);
  border-radius: 4px;
  outline: none;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-color);
  border-radius: 4px;
  cursor: pointer;
  transition: 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.page-btn.active {
  background: var(--primary-color);
  color: #fff;
  border-color: var(--primary-color);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ellipsis {
  padding: 0 4px;
  color: var(--text-secondary-color);
}
</style>
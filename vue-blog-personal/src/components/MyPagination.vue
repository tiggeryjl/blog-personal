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
  const realPage = Math.max(1, Math.min(page, totalPages.value))
  if (realPage === props.currentPage) return
  emit('update:currentPage', realPage)
  emit('change', realPage)
}

const handleSizeChange = (size) => {
  const newPageSize = Number(size)
  const newTotalPages = Math.ceil(props.total / newPageSize)
  const newCurrentPage = Math.min(props.currentPage, newTotalPages)

  emit('update:pageSize', newPageSize)
  emit('update:currentPage', newCurrentPage)
  emit('change', newCurrentPage)
}

const pageNumbers = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = props.currentPage
  const showSide = 1 // 当前页前后各显示2页，可调整

  if (total <= showSide * 2 + 3) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
    return pages
  }

  if (current <= showSide + 1) {
    for (let i = 1; i <= showSide * 2 + 1; i++) {
      pages.push(i)
    }
    if (total > showSide * 2 + 1) {
      pages.push('...')
      pages.push(total)
    }
    return pages
  }

  if (current >= total - showSide) {
    pages.push(1)
    if (total - showSide * 2 > 1) {
      pages.push('...')
    }
    for (let i = total - showSide * 2; i <= total; i++) {
      pages.push(i)
    }
    return pages
  }

  // 情况4：当前页在中间区域
  pages.push(1)
  pages.push('...')
  for (let i = current - showSide; i <= current + showSide; i++) {
    pages.push(i)
  }
  pages.push('...')
  pages.push(total)

  return pages
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

    <button class="page-btn" :disabled="currentPage === 1" @click="goPage(currentPage - 1)">
      上一页
    </button>

    <template v-for="item in pageNumbers" :key="item">
      <span v-if="item === '...'" class="ellipsis">···</span>
      <button v-else class="page-btn" :class="{ active: item === currentPage }" @click="goPage(item)">
        {{ item }}
      </button>
    </template>

    <button class="page-btn" :disabled="currentPage === totalPages" @click="goPage(currentPage + 1)">
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
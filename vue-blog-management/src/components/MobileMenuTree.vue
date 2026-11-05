<script setup>
import { ref } from 'vue';
import MobileMenuTree from './MobileMenuTree.vue';

defineOptions({ name: 'MobileMenuTree' });

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  level: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(['edit', 'add-child', 'delete']);
const expandedIds = ref(new Set());

const getNestedItems = (item) =>
  (item.children || []).filter((child) => child.menuType !== 2);

const getButtonItems = (item) =>
  (item.children || []).filter((child) => child.menuType === 2);

const hasChildren = (item) =>
  getNestedItems(item).length > 0 || getButtonItems(item).length > 0;

const isExpanded = (item) => expandedIds.value.has(String(item.id));

const toggleExpand = (item) => {
  if (!hasChildren(item)) return;
  const next = new Set(expandedIds.value);
  const key = String(item.id);
  if (next.has(key)) {
    next.delete(key);
  } else {
    next.add(key);
  }
  expandedIds.value = next;
};

const getTypeText = (type) => {
  if (type === 0) return '目录';
  if (type === 1) return '页面';
  return '按钮';
};

const getTypeTag = (type) => {
  if (type === 0) return 'warning';
  if (type === 1) return 'primary';
  return 'info';
};
</script>

<template>
  <div class="mobile-menu-tree" :class="{ nested: level > 0 }">
    <article
      v-for="item in props.items"
      :key="item.id || item.menuName"
      :class="{ disabled: item.status !== 1 }"
      class="mobile-menu-card"
    >
      <header
        class="mobile-menu-card-head"
        :class="{ expandable: hasChildren(item), expanded: isExpanded(item) }"
        :role="hasChildren(item) ? 'button' : undefined"
        :tabindex="hasChildren(item) ? 0 : undefined"
        :aria-expanded="hasChildren(item) ? isExpanded(item) : undefined"
        @click="toggleExpand(item)"
        @keydown.enter.prevent="toggleExpand(item)"
        @keydown.space.prevent="toggleExpand(item)"
      >
        <div class="mobile-menu-title">
          <el-icon v-if="hasChildren(item)" class="expand-arrow">
            <ArrowRight />
          </el-icon>
          <el-icon v-if="item.icon">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.menuName }}</span>
        </div>
        <div class="mobile-menu-tags">
          <el-tag size="small" :type="getTypeTag(item.menuType)">
            {{ getTypeText(item.menuType) }}
          </el-tag>
          <el-tag size="small" :type="item.status === 1 ? 'success' : 'danger'">
            {{ item.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
      </header>

      <div class="mobile-menu-meta">
        <span v-if="item.path"><b>路径</b>{{ item.path }}</span>
        <span v-if="item.perms"><b>权限</b><code>{{ item.perms }}</code></span>
        <span v-if="item.sort !== undefined"><b>排序</b>{{ item.sort }}</span>
      </div>

      <section v-if="isExpanded(item) && getButtonItems(item).length" class="menu-permission-section">
        <div class="menu-permission-title">按钮权限</div>
        <div class="menu-permission-list">
          <div
            v-for="button in getButtonItems(item)"
            :key="button.id"
            class="menu-permission-chip"
          >
            <button
              type="button"
              class="permission-main"
              @click="emit('edit', button)"
            >
              <span>{{ button.menuName }}</span>
              <code>{{ button.perms }}</code>
            </button>
            <button
              v-perm="'sys:menu:delete'"
              type="button"
              class="permission-delete"
              aria-label="删除按钮权限"
              @click="emit('delete', button)"
            >
              ×
            </button>
          </div>
        </div>
      </section>

      <MobileMenuTree
        v-if="isExpanded(item) && getNestedItems(item).length"
        :items="getNestedItems(item)"
        :level="props.level + 1"
        @edit="emit('edit', $event)"
        @add-child="emit('add-child', $event)"
        @delete="emit('delete', $event)"
      />

      <footer class="mobile-menu-actions">
        <el-button v-perm="'sys:menu:edit'" type="primary" link @click="emit('edit', item)">
          编辑
        </el-button>
        <el-button v-perm="'sys:menu:add'" type="success" link @click="emit('add-child', item)">
          新增下级
        </el-button>
        <el-button v-perm="'sys:menu:delete'" type="danger" link @click="emit('delete', item)">
          删除
        </el-button>
      </footer>
    </article>
  </div>
</template>

<style scoped>
.mobile-menu-tree {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mobile-menu-tree.nested {
  margin-top: 10px;
  padding-left: 10px;
  border-left: 2px solid var(--el-color-primary-light-8);
}

.mobile-menu-card {
  padding: 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: linear-gradient(145deg, var(--el-bg-color, #fff), var(--el-fill-color-extra-light));
  box-shadow: 0 3px 12px rgba(31, 45, 61, 0.06);
}

.mobile-menu-card.disabled {
  opacity: 0.72;
}

.mobile-menu-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.mobile-menu-card-head.expandable {
  cursor: pointer;
}

.mobile-menu-card-head.expandable:focus-visible {
  outline: 2px solid var(--el-color-primary-light-5);
  outline-offset: 3px;
  border-radius: 6px;
}

.expand-arrow {
  flex: 0 0 auto;
  color: var(--el-text-color-secondary);
  transition: transform 0.2s ease;
}

.mobile-menu-card-head.expanded .expand-arrow {
  transform: rotate(90deg);
}

.mobile-menu-title {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
  color: var(--el-text-color-primary);
  font-size: 15px;
  font-weight: 600;
}

.mobile-menu-title .el-icon {
  flex: 0 0 auto;
  color: var(--el-color-primary);
}

.mobile-menu-tags {
  display: flex;
  flex: 0 0 auto;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 4px;
}

.mobile-menu-meta {
  display: flex;
  margin-top: 9px;
  flex-wrap: wrap;
  gap: 6px;
}

.mobile-menu-meta > span {
  display: inline-flex;
  max-width: 100%;
  align-items: center;
  gap: 5px;
  padding: 4px 7px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-size: 11px;
  overflow-wrap: anywhere;
}

.mobile-menu-meta b {
  flex: 0 0 auto;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.mobile-menu-meta code,
.permission-main code {
  color: inherit;
  font-family: inherit;
  font-size: inherit;
  overflow-wrap: anywhere;
}

.menu-permission-section {
  margin-top: 10px;
  padding-top: 9px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.menu-permission-title {
  margin-bottom: 7px;
  color: var(--el-text-color-secondary);
  font-size: 11px;
  font-weight: 500;
}

.menu-permission-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.menu-permission-chip {
  display: inline-flex;
  max-width: 100%;
  align-items: center;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 999px;
  background: var(--el-bg-color, #fff);
}

.permission-main {
  display: inline-flex;
  min-width: 0;
  align-items: center;
  gap: 5px;
  padding: 5px 8px;
  border: 0;
  background: transparent;
  color: var(--el-text-color-regular);
  font-size: 11px;
  cursor: pointer;
}

.permission-main span {
  flex: 0 0 auto;
}

.permission-main code {
  min-width: 0;
  overflow: hidden;
  color: var(--el-color-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.permission-delete {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-left: 1px solid var(--el-border-color-lighter);
  background: transparent;
  color: var(--el-color-danger);
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
}

.mobile-menu-actions {
  display: flex;
  margin-top: 9px;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 4px;
}

.mobile-menu-actions .el-button {
  margin: 0 !important;
  padding: 3px 6px;
  font-size: 12px;
}
</style>

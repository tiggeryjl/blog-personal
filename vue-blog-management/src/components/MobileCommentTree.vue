<script setup>
import { computed, ref } from 'vue';
import {
  COMMENT_STATUS,
  getCommentStatusText,
  getCommentStatusType,
  getMessageTypeText,
} from '@/constants/commentConstants';
import { CaretRight, ChatLineRound, Check, Delete, Hide, Top } from '@element-plus/icons-vue';

const props = defineProps({
  items: { type: Array, default: () => [] },
  selectedIds: { type: Array, default: () => [] },
  showSourceId: { type: Boolean, default: true },
  showMsgType: { type: Boolean, default: false },
});

const emit = defineEmits(['reply', 'audit', 'top', 'delete', 'toggle-select']);
const expandedIds = ref(new Set());

const hasChildren = (item) => Boolean(item.children?.length);
const isExpanded = (item) => expandedIds.value.has(String(item.id));
const isSelected = (item) => props.selectedIds.some((id) => String(id) === String(item.id));

const visibleItems = computed(() => {
  const result = [];
  const walk = (items, level) => {
    (items || []).forEach((item) => {
      result.push({ item, level: Math.min(level, 2) });
      if (hasChildren(item) && isExpanded(item)) {
        walk(item.children, level + 1);
      }
    });
  };
  walk(props.items, 0);
  return result;
});

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

</script>

<template>
  <div class="mobile-comment-tree">
    <article
      v-for="{ item, level } in visibleItems"
      :key="item.id"
      class="mobile-comment-card"
      :class="[
        `depth-${level}`,
        { 'is-hidden': item.status === COMMENT_STATUS.HIDDEN },
      ]"
      :style="{ '--comment-depth': level }"
    >
      <header
        class="comment-card-head"
        :class="{ expandable: hasChildren(item), expanded: isExpanded(item) }"
        :role="hasChildren(item) ? 'button' : undefined"
        :tabindex="hasChildren(item) ? 0 : undefined"
        :aria-expanded="hasChildren(item) ? isExpanded(item) : undefined"
        @click="toggleExpand(item)"
        @keydown.enter.prevent="toggleExpand(item)"
        @keydown.space.prevent="toggleExpand(item)"
      >
        <div class="comment-user">
          <el-icon v-if="hasChildren(item)" class="expand-arrow">
            <CaretRight />
          </el-icon>
          <el-avatar :size="36" :src="item.userAvatar || ''">
            {{ item.userNickname ? item.userNickname.charAt(0) : '' }}
          </el-avatar>
          <div class="user-text">
            <div class="user-name-line">
              <span>{{ item.userNickname || '—' }}</span>
              <el-tag v-if="item.admin" size="small" type="primary" effect="light" round>博主</el-tag>
            </div>
            <small>ID: {{ item.userId }}</small>
          </div>
        </div>

        <el-checkbox
          class="comment-select"
          :model-value="isSelected(item)"
          @click.stop
          @change="emit('toggle-select', { id: item.id, checked: $event })"
        />
      </header>

      <div class="comment-content">
        {{ item.content }}
        <div class="comment-tags">
          <el-tag v-if="item.parentId && item.parentId !== 0" size="small" type="info">
            回复 @{{ item.replyUserNickname || item.parentNickname || '—' }}
          </el-tag>
          <el-tag v-if="item.isOrphan" size="small" type="warning">原主楼不在本页</el-tag>
        </div>
      </div>

      <div class="comment-meta-grid">
        <span v-if="showSourceId && item.sourceTitle">
          <b>来源</b>{{ item.sourceTitle }}
        </span>
        <span v-if="showSourceId && item.sourceId">
          <b>来源ID</b>{{ item.sourceId }}
        </span>
        <span v-if="showMsgType">
          <b>类型</b>{{ getMessageTypeText(item.msgType) }}
        </span>
        <span>
          <b>状态</b>
          <el-tag size="small" :type="getCommentStatusType(item.status)">
            {{ getCommentStatusText(item.status) }}
          </el-tag>
        </span>
        <span>
          <b>置顶</b>
          <el-tag size="small" :type="item.isTop === 1 ? 'warning' : 'info'">
            {{ item.isTop === 1 ? '已置顶' : '未置顶' }}
          </el-tag>
        </span>
        <span><b>互动</b>赞 {{ item.likeNum ?? 0 }} / 回复 {{ item.replyCount ?? 0 }}</span>
        <span v-if="item.location"><b>位置</b>{{ item.location }}</span>
        <span v-if="item.browser || item.os"><b>设备</b>{{ item.browser || '' }} {{ item.os || '' }}</span>
        <span><b>时间</b>{{ item.createTime || '—' }}</span>
      </div>

      <div v-if="hasChildren(item)" class="reply-toggle-hint">
        {{ isExpanded(item) ? '收起回复' : `查看 ${item.children.length} 条回复` }}
      </div>

      <footer class="comment-actions">
        <el-button v-perm="'sys:comment:audit'" type="success" link :icon="ChatLineRound" @click="emit('reply', item)">
          回复
        </el-button>
        <el-button
          v-perm="'sys:comment:audit'"
          v-if="item.status === COMMENT_STATUS.HIDDEN"
          type="success"
          link
          :icon="Check"
          @click="emit('audit', item, COMMENT_STATUS.NORMAL)"
        >
          显示
        </el-button>
        <el-button
          v-else
          v-perm="'sys:comment:audit'"
          type="warning"
          link
          :icon="Hide"
          @click="emit('audit', item, COMMENT_STATUS.HIDDEN)"
        >
          隐藏
        </el-button>
        <el-button v-perm="'sys:comment:audit'" type="primary" link :icon="Top" @click="emit('top', item)">
          {{ item.isTop === 1 ? '取消置顶' : '置顶' }}
        </el-button>
        <el-button v-perm="'sys:comment:delete'" type="danger" link :icon="Delete" @click="emit('delete', item)">
          删除
        </el-button>
      </footer>
    </article>
  </div>
</template>

<style scoped>
.mobile-comment-tree {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mobile-comment-card {
  width: calc(100% - var(--comment-depth) * 10px);
  margin-left: calc(var(--comment-depth) * 10px);
  padding: 12px;
  box-sizing: border-box;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: linear-gradient(145deg, var(--el-bg-color, #fff), var(--el-fill-color-extra-light));
  box-shadow: 0 3px 12px rgba(31, 45, 61, 0.06);
}

.mobile-comment-card.depth-1 {
  border-left: 3px solid var(--el-color-primary-light-5);
}

.mobile-comment-card.depth-2 {
  border-left: 3px solid var(--el-color-warning-light-5);
}

.mobile-comment-card.is-hidden {
  opacity: 0.8;
}

.comment-card-head {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding-right: 30px;
}

.comment-card-head.expandable {
  cursor: pointer;
}

.comment-user {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.expand-arrow {
  flex: 0 0 auto;
  color: var(--el-text-color-secondary);
  transition: transform 0.2s ease;
}

.comment-card-head.expanded .expand-arrow {
  transform: rotate(90deg);
}

.user-text {
  min-width: 0;
}

.user-name-line {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.user-text small {
  color: var(--el-text-color-secondary);
  font-size: 11px;
}

.comment-select {
  position: absolute;
  top: -3px;
  right: 0;
}

.comment-content {
  margin-top: 10px;
  color: var(--el-text-color-primary);
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-tags {
  display: flex;
  margin-top: 7px;
  flex-wrap: wrap;
  gap: 5px;
}

.comment-meta-grid {
  display: grid;
  margin-top: 9px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}

.comment-meta-grid > span {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 4px;
  padding: 5px 7px;
  border-radius: 7px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-size: 11px;
  overflow-wrap: anywhere;
}

.comment-meta-grid b {
  flex: 0 0 auto;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.reply-toggle-hint {
  margin-top: 8px;
  color: var(--el-color-primary);
  font-size: 11px;
}

.comment-actions {
  display: flex;
  margin-top: 9px;
  padding-top: 8px;
  flex-wrap: wrap;
  gap: 4px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.comment-actions .el-button {
  height: 28px;
  margin: 0 !important;
  padding: 0 8px !important;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 999px;
  background: var(--el-bg-color, #fff);
  font-size: 12px;
}
</style>

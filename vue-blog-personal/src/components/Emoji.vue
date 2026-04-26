<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'close'])

const emojiList = ref([
  { code: '😀', name: '微笑' },
  { code: '😁', name: '呲牙' },
  { code: '😂', name: '笑哭' },
  { code: '🤣', name: '滚地笑' },
  { code: '😊', name: '害羞' },
  { code: '😇', name: '天使' },
  { code: '😍', name: '花痴' },
  { code: '🤩', name: '眼冒星' },
  { code: '😎', name: '酷' },
  { code: '🥳', name: '庆祝' },
  { code: '😜', name: '调皮' },
  { code: '🤪', name: '疯癫' },
  { code: '😝', name: '歪脸' },
  { code: '🤗', name: '抱抱' },
  { code: '🤔', name: '思考' },
  { code: '🤭', name: '捂嘴笑' },
  { code: '😘', name: '飞吻' },
  { code: '🥰', name: '甜' },
  { code: '😴', name: '睡觉' },
  { code: '🥺', name: '可怜' },
  { code: '😭', name: '大哭' },
  { code: '😗', name: '事不关己' },
  { code: '😚', name: '愉快' },
  { code: '😙', name: '亲亲' },
  { code: '😛', name: '吐舌' },
  { code: '🤑', name: '见钱眼开' },
  { code: '🤠', name: '牛仔' },
  { code: '😢', name: '伤心' }
])

const hoverEmoji = ref(null)
const tooltipPosition = ref({ top: 0, left: 0 })
let cleanupMouseMove = null

const selectEmoji = (emoji) => {
  emit('select', emoji.code)
}

const closePanel = () => {
  hoverEmoji.value = null
  emit('close')
}

const showTooltip = (emoji, event) => {
  hoverEmoji.value = emoji
  const rect = event.target.closest('span').getBoundingClientRect()
  tooltipPosition.value = {
    top: rect.top - 20,   // 显示在表情上方 30px
    left: rect.left + rect.width / 2
  }
}

const hideTooltip = () => {
  hoverEmoji.value = null
}

watch(() => props.show, (newVal, oldVal) => {
  if (oldVal === true && newVal === false) {
    hoverEmoji.value = null
  }
})
</script>

<template>
  <div v-if="show" class="emoji-panel" @click.stop>
    <span v-for="emoji in emojiList" :key="emoji.code" @click="selectEmoji(emoji)"
      @mouseenter="showTooltip(emoji, $event)" @mouseleave="hideTooltip">
      {{ emoji.code }}
    </span>
  </div>

  <Teleport to="body">
    <div v-if="hoverEmoji" class="global-emoji-tooltip"
      :style="{ top: tooltipPosition.top + 'px', left: tooltipPosition.left + 'px' }">
      {{ hoverEmoji.name }}
      <div class="tooltip-arrow"></div>
    </div>
  </Teleport>
</template>

<style scoped>
.emoji-panel {
  position: absolute;
  left: 12px;
  bottom: 46px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 10px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  max-width: 280px;
  max-height: 160px;
  overflow-y: auto;
  z-index: 99;
}

.emoji-panel span {
  position: relative;
  font-size: 18px;
  cursor: pointer;
  padding: 2px;
}

.emoji-panel span:hover {
  transform: scale(1.2);
}
</style>

<style>
.global-emoji-tooltip {
  position: fixed;
  background: #444;
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 9999;
  transform: translateX(-50%);
  margin-top: -8px;
}

.global-emoji-tooltip .tooltip-arrow {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 6px solid #444;
}
</style>
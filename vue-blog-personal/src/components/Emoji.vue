<script setup>
import { ref, watch } from 'vue'

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
  { code: '😗', name: '[汪汪]' },
  { code: '😚', name: '愉快' },
  { code: '😙', name: '亲亲' },
  { code: '😛', name: '吐舌' },
  { code: '🤑', name: '见钱眼开' },
  { code: '🤠', name: '牛仔' },
  { code: '😢', name: '伤心' },
])
const hoverEmoji = ref(null)

// 选中表情插入输入框
const selectEmoji = (emoji) => {
  emit('select', emoji.code)
}

const closePanel = () => {
  hoverEmoji.value = null
  emit('close')
}

watch(() => props.show, (newVal, oldVal) => {
  if (oldVal === true && newVal === false) {
    hoverEmoji.value = null
  }
})
</script>

<template>
  <!-- <div class="comment-list-widget">
    <div class="emoji-btn" @click="showEmoji = !showEmoji">
      <font-awesome-icon :icon="['fa', 'face-smile']" />
    </div>

    <div v-if="showEmoji" class="emoji-panel">
      <span v-for="emoji in emojiList" :key="emoji.code" @click="selectEmoji(emoji)" @mouseenter="hoverEmoji = emoji"
        @mouseleave="hoverEmoji = null">
        {{ emoji.code }}

        <div v-if="hoverEmoji === emoji" class="emoji-tooltip">
          {{ hoverEmoji.name }}
        </div>
      </span>
    </div>
  </div> -->
  <div v-if="show" class="emoji-panel" @click.stop>
    <span v-for="emoji in emojiList" :key="emoji.code" @click="selectEmoji(emoji)" @mouseenter="hoverEmoji = emoji"
      @mouseleave="hoverEmoji = null">
      {{ emoji.code }}
      <div v-if="hoverEmoji === emoji" class="emoji-tooltip">
        {{ hoverEmoji.name }}
      </div>
    </span>
  </div>
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

.emoji-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: #444;
  color: #fff;
  font-size: 12px;
  padding: 3px 6px;
  border-radius: 4px;
  white-space: nowrap;
  pointer-events: none;
  margin-bottom: 4px;
  z-index: 100;
}

.emoji-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 3px;
  border-style: solid;
  border-color: #444 transparent transparent transparent;
}
</style>
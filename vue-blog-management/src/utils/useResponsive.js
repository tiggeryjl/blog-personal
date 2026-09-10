import { onBeforeUnmount, onMounted, ref } from 'vue';

// 移动端断点（px），需与样式文件中的 @media (max-width: 768px) 保持一致
export const MOBILE_BREAKPOINT = 768;

/**
 * 监听视口宽度，判断当前是否处于移动端布局
 * @param {number} breakpoint 断点宽度，默认 768
 * @returns {{ isMobile: import('vue').Ref<boolean> }}
 */
export function useMobile(breakpoint = MOBILE_BREAKPOINT) {
  const query = `(max-width: ${breakpoint}px)`;
  const isMobile = ref(typeof window !== 'undefined' && window.matchMedia(query).matches);

  let mediaQuery = null;
  const handleChange = (event) => {
    isMobile.value = event.matches;
  };

  onMounted(() => {
    mediaQuery = window.matchMedia(query);
    isMobile.value = mediaQuery.matches;
    // 旧版浏览器只支持 addListener，这里做兼容处理
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange);
    } else {
      mediaQuery.addListener(handleChange);
    }
  });

  onBeforeUnmount(() => {
    if (!mediaQuery) return;
    if (mediaQuery.removeEventListener) {
      mediaQuery.removeEventListener('change', handleChange);
    } else {
      mediaQuery.removeListener(handleChange);
    }
  });

  return { isMobile };
}

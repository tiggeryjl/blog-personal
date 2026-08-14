import { defineStore } from 'pinia';
export const useNoticeStore = defineStore('notice', {
  state: () => ({ unreadCount: 0 }),
  actions: {
    setCount(val) {
      this.unreadCount = val;
    },
    addCount(num = 1) {
      this.unreadCount += num;
    },
    clearCount() {
      this.unreadCount = 0;
    },
  },
});

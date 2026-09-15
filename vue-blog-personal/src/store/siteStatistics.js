import { defineStore } from 'pinia';

const toCount = (value) => Number(value) || 0;

export const useSiteStatisticsStore = defineStore('siteStatistics', {
  state: () => ({
    onlineVisitor: 0,
    todayView: 0,
    totalView: 0,
    totalVisitor: 0,
    articleCount: 0,
    runningSeconds: 0,
    loaded: false,
  }),

  actions: {
    updateStatistics(statistics) {
      this.onlineVisitor = toCount(statistics.onlineVisitor);
      this.todayView = toCount(statistics.todayView);
      this.totalView = toCount(statistics.totalView);
      this.totalVisitor = toCount(statistics.totalVisitor);
      this.articleCount = toCount(statistics.articleCount);
      this.runningSeconds = toCount(statistics.runningSeconds);
      this.loaded = true;
    },

    tickRunningTime() {
      if (this.loaded) this.runningSeconds += 1;
    },
  },
});

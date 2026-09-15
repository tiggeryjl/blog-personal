export const USER_NOTICE_WS_PATH = import.meta.env.VITE_USER_NOTICE_WS_URL || '/ws/user/notice';

export const USER_NOTICE_RECONNECT_DELAY = 3000;

export const USER_NOTICE_HEARTBEAT_INTERVAL = 25000;

export const SITE_STATISTICS_MESSAGE_TYPE = 'siteStatistics';

export const USER_NOTICE_TYPE_MAP = {
  like: 'success',
  comment: 'info',
  reply: 'info',
  system: 'warning',
};

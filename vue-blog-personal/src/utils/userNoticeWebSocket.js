import { onBeforeUnmount, watch } from 'vue';
import { ElNotification } from 'element-plus';
import { useUserStore } from '@/store/userloginstatus';
import { useSiteStatisticsStore } from '@/store/siteStatistics';
import {
  SITE_STATISTICS_MESSAGE_TYPE,
  USER_NOTICE_HEARTBEAT_INTERVAL,
  USER_NOTICE_RECONNECT_DELAY,
  USER_NOTICE_TYPE_MAP,
  USER_NOTICE_WS_PATH,
} from '@/constants/userNoticeConstants';

const HEARTBEAT_PING = 'ping';
const HEARTBEAT_PONG = 'pong';

const buildWebSocketUrl = (token) => {
  const baseUrl = `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}`;
  const url = new URL(USER_NOTICE_WS_PATH, baseUrl);
  url.protocol = url.protocol === 'https:' ? 'wss:' : url.protocol === 'http:' ? 'ws:' : url.protocol;
  if (token) url.searchParams.set('token', token);
  return url.toString();
};

const showNotice = (notice) => {
  const summary = [notice.operatorName, notice.actionText, notice.articleTitle]
    .filter(Boolean)
    .join(' ');

  ElNotification({
    title: notice.title || '消息通知',
    message: notice.content || summary || '您收到一条新消息',
    type: USER_NOTICE_TYPE_MAP[notice.type] || 'info',
    duration: 5000,
  });
};

export const useUserNoticeWebSocket = () => {
  const userStore = useUserStore();
  const siteStatisticsStore = useSiteStatisticsStore();
  let socket = null;
  let reconnectTimer = null;
  let heartbeatTimer = null;
  let waitingPong = false;
  let destroyed = false;

  const clearReconnectTimer = () => {
    if (!reconnectTimer) return;
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  };

  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer);
      heartbeatTimer = null;
    }
    waitingPong = false;
  };

  const closeSocket = () => {
    clearReconnectTimer();
    stopHeartbeat();
    if (!socket) return;

    const currentSocket = socket;
    socket = null;
    currentSocket.onopen = null;
    currentSocket.onmessage = null;
    currentSocket.onclose = null;
    currentSocket.onerror = null;
    if (currentSocket.readyState === WebSocket.OPEN || currentSocket.readyState === WebSocket.CONNECTING) {
      currentSocket.close();
    }
  };

  const sendMessage = (message) => {
    if (!socket || socket.readyState !== WebSocket.OPEN) return false;
    socket.send(typeof message === 'string' ? message : JSON.stringify(message));
    return true;
  };

  const startHeartbeat = () => {
    stopHeartbeat();
    heartbeatTimer = setInterval(() => {
      if (!socket || socket.readyState !== WebSocket.OPEN) return;
      if (waitingPong) {
        socket.close();
        return;
      }
      waitingPong = true;
      socket.send(HEARTBEAT_PING);
    }, USER_NOTICE_HEARTBEAT_INTERVAL);
  };

  const scheduleReconnect = (connect) => {
    if (destroyed || reconnectTimer) return;
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null;
      connect();
    }, USER_NOTICE_RECONNECT_DELAY);
  };

  const connect = () => {
    const token = userStore.user_token;
    if (destroyed) return;
    if (!('WebSocket' in window)) {
      console.error('当前浏览器不支持WebSocket');
      return;
    }
    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
      return;
    }

    clearReconnectTimer();
    const currentSocket = new WebSocket(buildWebSocketUrl(token));
    socket = currentSocket;

    currentSocket.onopen = () => {
      if (socket !== currentSocket) return;
      console.log('用户通知WebSocket连接成功');
      startHeartbeat();
    };

    currentSocket.onmessage = (event) => {
      if (event.data === HEARTBEAT_PONG) {
        waitingPong = false;
        return;
      }
      try {
        const notice = JSON.parse(event.data);
        if (notice?.messageType === SITE_STATISTICS_MESSAGE_TYPE) {
          siteStatisticsStore.updateStatistics(notice);
          return;
        }
        showNotice(notice);
      } catch (error) {
        console.warn('用户通知WebSocket消息解析失败:', error);
      }
    };

    currentSocket.onclose = () => {
      if (socket !== currentSocket) return;
      socket = null;
      stopHeartbeat();
      console.log('用户通知WebSocket连接断开');
      scheduleReconnect(connect);
    };

    currentSocket.onerror = () => {
      currentSocket.close();
    };
  };

  const stopWatch = watch(
    () => userStore.user_token,
    (token, oldToken) => {
      if (token === oldToken) return;
      closeSocket();
      connect();
    },
    { immediate: true }
  );

  onBeforeUnmount(() => {
    destroyed = true;
    stopWatch();
    closeSocket();
  });

  return { sendMessage, connect, close: closeSocket };
};

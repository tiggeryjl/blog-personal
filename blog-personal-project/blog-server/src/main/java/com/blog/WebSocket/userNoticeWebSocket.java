package com.blog.WebSocket;

import com.alibaba.fastjson.JSON;
import com.blog.config.SpringContextHolder;
import com.blog.pojo.dto.SysNoticeDTO;
import com.blog.pojo.vo.SiteStatisticsVO;
import com.blog.service.HomeService;
import com.blog.service.JwtService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/user/notice")
public class userNoticeWebSocket {

    //存放所有WebSocket握手成功的会话对象
    private static final Set<Session> ONLINE_SESSIONS = ConcurrentHashMap.newKeySet();

    private static final String HEARTBEAT_PING = "ping";

    private static final String HEARTBEAT_PONG = "pong";

    private Long userId;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        resolveUser(session);
        ONLINE_SESSIONS.add(session);
        log.info("【WS】访客连接建立成功，用户id：{}，当前在线数量：{}", userId, getOnlineCount());
        broadcastSiteStatistics();
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        ONLINE_SESSIONS.remove(session);
        log.info("【WS】访客连接断开，用户id：{}，当前在线数量：{}", userId, getOnlineCount());
        broadcastSiteStatistics();
    }

    /**
     * 接收前端消息
     * @param msg
     * @param session
     */
    @OnMessage
    public void onMessage(String msg, Session session) {
        if (!HEARTBEAT_PING.equalsIgnoreCase(msg.trim())) {
            log.debug("【WS】收到用户{}消息", userId);
            return;
        }
        try {
            session.getBasicRemote().sendText(HEARTBEAT_PONG);
            sendSiteStatistics(session);
        } catch (IOException e) {
            log.error("【WS】用户{}心跳响应发送失败", userId, e);
            ONLINE_SESSIONS.remove(session);
            closeSession(session);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("【WS】用户{}连接异常", userId, throwable);
        ONLINE_SESSIONS.remove(session);
        broadcastSiteStatistics();
    }

    /**
     * 统计当前在线的用户端数量，游客和登录用户统一计数
     */
    public static long getOnlineCount() {
        return ONLINE_SESSIONS.stream().filter(Session::isOpen).count();
    }

    /**
     * 推送消息
     *
     * @param message
     */
    public void broadcast(SysNoticeDTO message) {
        broadcastPayload(message);
    }

    /**
     * 向所有在线访客推送站点统计
     */
    public void broadcastSiteStatistics() {
        try {
            HomeService homeService = SpringContextHolder.getBean(HomeService.class);
            broadcastPayload(homeService.getSiteStatistics());
        } catch (Exception e) {
            log.error("【WS】推送站点统计失败", e);
        }
    }

    /**
     * 单点推送，发给指定用户
     */
    public void sendToOne(Long targetUserId, SysNoticeDTO message) {
        List<Session> sessions = ONLINE_SESSIONS.stream()
                .filter(Session::isOpen)
                .filter(item -> targetUserId.equals(item.getUserProperties().get("userId")))
                .toList();
        if (sessions.isEmpty()) {
            log.warn("用户{}不在线，消息无法推送", targetUserId);
            return;
        }
        String json = JSON.toJSONString(message);
        for (Session userSession : sessions) {
            try {
                userSession.getBasicRemote().sendText(json);
            } catch (IOException e) {
                log.error("向用户{}单点推送失败", targetUserId, e);
                ONLINE_SESSIONS.remove(userSession);
            }
        }
    }

    /**
     * 向指定会话推送最新站点统计
     */
    private void sendSiteStatistics(Session session) throws IOException {
        try {
            HomeService homeService = SpringContextHolder.getBean(HomeService.class);
            SiteStatisticsVO statistics = homeService.getSiteStatistics();
            session.getBasicRemote().sendText(JSON.toJSONString(statistics));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("【WS】获取站点统计失败", e);
        }
    }

    /**
     * 广播WebSocket消息
     */
    private void broadcastPayload(Object message) {
        List<Session> sessions = new ArrayList<>(ONLINE_SESSIONS);
        if (sessions.isEmpty()) {
            return;
        }
        String json;
        try {
            json = JSON.toJSONString(message);
        } catch (Exception e) {
            log.error("【WS】消息序列化为JSON失败", e);
            return;
        }
        for (Session onlineSession : sessions) {
            if (!onlineSession.isOpen()) {
                ONLINE_SESSIONS.remove(onlineSession);
                continue;
            }
            try {
                onlineSession.getBasicRemote().sendText(json);
            } catch (IOException e) {
                log.error("【WS】消息推送失败，准备移除异常会话", e);
                ONLINE_SESSIONS.remove(onlineSession);
            }
        }
    }

    /**
     * token为可选参数，校验失败时按游客连接处理
     */
    private void resolveUser(Session session) {
        List<String> tokens = session.getRequestParameterMap().get("token");
        String token = tokens == null || tokens.isEmpty() ? null : tokens.get(0);
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            JwtService jwtService = SpringContextHolder.getBean(JwtService.class);
            if (!jwtService.validateToken(token)) {
                log.debug("【WS】访客token已失效，按未登录访客处理");
                return;
            }
            userId = jwtService.getUserId(token);
            session.getUserProperties().put("userId", userId);
        } catch (Exception e) {
            log.debug("【WS】访客token解析失败，按未登录访客处理");
        }
    }

    /**
     * 关闭会话
     *
     * @param session
     */
    private void closeSession(Session session) {
        try {
            if (session.isOpen()) session.close();
        } catch (Exception ignored) {
        }
    }

}

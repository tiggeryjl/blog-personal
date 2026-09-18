package com.blog.WebSocket;

import com.alibaba.fastjson.JSON;
import com.blog.config.SpringContextHolder;
import com.blog.pojo.dto.SysNoticeDTO;
import com.blog.pojo.vo.AdminOnlineCountVO;
import com.blog.service.JwtService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@ServerEndpoint("/ws/admin/notice")
public class AdminNoticeWebSocket {

    //存放所有WebSocket握手成功的会话对象
    private static final Map<Long, Set<Session>> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    private static final Object ONLINE_STATE_LOCK = new Object();

    private static final String ONLINE_COUNT_SUBSCRIBE = "subscribeOnlineCount";

    private static final String HEARTBEAT_PING = "ping";

    private static final String HEARTBEAT_PONG = "pong";

    private static final String ONLINE_COUNT_SUBSCRIBED = "onlineCountSubscribed";

    private static long onlineCountVersion = 0L;

    private Session session;

    private Long adminId;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        String query = session.getQueryString();
        if (query == null || !query.contains("token=")) {
            closeSession(session);
            return;
        }
        Map<String, String> params = parseQuery(query);
        String token = params.get("token");
        if (token == null || token.isBlank()) {
            closeSession(session);
            return;
        }

        JwtService jwtService = SpringContextHolder.getBean(JwtService.class);
        if (!jwtService.validateToken(token)) {
            log.warn("【WS】token校验失败，关闭连接");
            closeSession(session);
            return;
        }
        adminId = jwtService.getUserId(token);
        session.getUserProperties().put("adminId", adminId);
        //同一管理员多端登录时分别保存会话，避免互相覆盖
        AdminOnlineCountVO onlineCountMessage = addSession(adminId, session);
        log.info("【WS】管理员{}连接建立成功，当前在线数量：{}", adminId,
                onlineCountMessage.getOnlineCount());
        broadcastOnlineCount(onlineCountMessage);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        AdminOnlineCountVO onlineCountMessage = removeSession(adminId, session);
        log.info("【WS】管理员{}连接断开，当前在线数量：{}", adminId, getOnlineCount());
        broadcastOnlineCount(onlineCountMessage);
    }

    /**
     * 接受前端消息
     * @param msg
     * @param session
     */
    @OnMessage
    public void onMessage(String msg, Session session) {
        String message = msg.trim();
        if (ONLINE_COUNT_SUBSCRIBE.equalsIgnoreCase(message)) {
            synchronized (ONLINE_STATE_LOCK) {
                session.getUserProperties().put(ONLINE_COUNT_SUBSCRIBED, true);
            }
            try {
                sendOnlineCount(session);
            } catch (IOException | RuntimeException e) {
                log.error("【WS】管理员{}在线数初始消息发送失败", adminId, e);
                handleFailedSession(session);
            }
            return;
        }
        if (!HEARTBEAT_PING.equalsIgnoreCase(message)) {
            log.debug("【WS】收到管理员{}消息", adminId);
            return;
        }
        try {
            sendText(session, HEARTBEAT_PONG);
            sendOnlineCount(session);
        } catch (IOException | RuntimeException e) {
            log.error("【WS】管理员{}心跳响应发送失败", adminId, e);
            handleFailedSession(session);
        }
    }

    @OnError
    public void onError(Session session,Throwable throwable){
        log.error("【WS】异常",throwable);
        handleFailedSession(session);
    }

    /**
     * 统计当前在线的管理端数量（同一管理员多端登录分别计数）
     *
     * @return 在线会话数量
     */
    public static long getOnlineCount() {
        synchronized (ONLINE_STATE_LOCK) {
            return countSessionsLocked();
        }
    }

    /**
     * 推送消息
     *
     * @param message
     */
    public void broadcast(SysNoticeDTO message) {
        broadcastPayload(message, false);
    }

    /**
     * 向所有在线管理端推送最新在线数量
     */
    private void broadcastOnlineCount(AdminOnlineCountVO message) {
        if (message != null) {
            broadcastPayload(message, true);
        }
    }

    /**
     * 向指定会话补发最新在线数量
     */
    private void sendOnlineCount(Session session) throws IOException {
        sendText(session, JSON.toJSONString(getOnlineCountMessage()));
    }

    /**
     * 广播WebSocket消息
     */
    private void broadcastPayload(Object message, boolean subscribedOnly) {
        List<Session> sessions = getSessionsSnapshot(subscribedOnly);
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
        AdminOnlineCountVO correctionMessage = null;
        for (Session onlineSession : sessions) {
            if (!sendTextSafely(onlineSession, json)) {
                Long aid = (Long) onlineSession.getUserProperties().get("adminId");
                AdminOnlineCountVO removedMessage = removeSession(aid, onlineSession);
                if (removedMessage != null) {
                    correctionMessage = removedMessage;
                }
                closeSession(onlineSession);
            }
        }
        broadcastOnlineCount(correctionMessage);
    }

    /**
     * 单点推送，发给指定管理员
     */
    public void sendToOne(Long targetAdminId, SysNoticeDTO message){
        List<Session> sessions = getAdminSessionsSnapshot(targetAdminId);
        if (sessions.isEmpty()) {
            log.warn("管理员{}不在线，消息无法推送",targetAdminId);
            return;
        }
        String json = JSON.toJSONString(message);
        AdminOnlineCountVO correctionMessage = null;
        //同一管理员可能多端登录，每个会话都要推送
        for (Session targetSession : sessions) {
            if (!sendTextSafely(targetSession, json)) {
                AdminOnlineCountVO removedMessage = removeSession(targetAdminId, targetSession);
                if (removedMessage != null) {
                    correctionMessage = removedMessage;
                }
                closeSession(targetSession);
            }
        }
        broadcastOnlineCount(correctionMessage);
    }

    /**
     * 原子添加管理端会话并生成当前在线数快照
     */
    private AdminOnlineCountVO addSession(Long adminId, Session session) {
        synchronized (ONLINE_STATE_LOCK) {
            Set<Session> sessions = ONLINE_SESSIONS.computeIfAbsent(adminId,
                    key -> ConcurrentHashMap.newKeySet());
            if (sessions.add(session)) {
                onlineCountVersion++;
            }
            return buildOnlineCountMessageLocked();
        }
    }

    /**
     * 移除管理员的指定会话，该管理员没有会话后清理掉对应的key
     *
     * @param adminId 管理员id
     * @param session 需要移除的会话
     * @return 在线数有变化时返回最新快照，否则返回null
     */
    private AdminOnlineCountVO removeSession(Long adminId, Session session) {
        if (adminId == null) {
            return null;
        }
        synchronized (ONLINE_STATE_LOCK) {
            Set<Session> sessions = ONLINE_SESSIONS.get(adminId);
            if (sessions == null || !sessions.remove(session)) {
                return null;
            }
            if (sessions.isEmpty()) {
                ONLINE_SESSIONS.remove(adminId, sessions);
            }
            onlineCountVersion++;
            return buildOnlineCountMessageLocked();
        }
    }

    private AdminOnlineCountVO getOnlineCountMessage() {
        synchronized (ONLINE_STATE_LOCK) {
            return buildOnlineCountMessageLocked();
        }
    }

    private AdminOnlineCountVO buildOnlineCountMessageLocked() {
        return AdminOnlineCountVO.builder()
                .onlineCount(countSessionsLocked())
                .version(onlineCountVersion)
                .build();
    }

    private static long countSessionsLocked() {
        return ONLINE_SESSIONS.values().stream()
                .mapToLong(Set::size)
                .sum();
    }

    private List<Session> getSessionsSnapshot(boolean subscribedOnly) {
        synchronized (ONLINE_STATE_LOCK) {
            return ONLINE_SESSIONS.values().stream()
                    .flatMap(Set::stream)
                    .filter(item -> !subscribedOnly
                            || Boolean.TRUE.equals(item.getUserProperties().get(ONLINE_COUNT_SUBSCRIBED)))
                    .toList();
        }
    }

    private List<Session> getAdminSessionsSnapshot(Long targetAdminId) {
        synchronized (ONLINE_STATE_LOCK) {
            Set<Session> sessions = ONLINE_SESSIONS.get(targetAdminId);
            return sessions == null ? List.of() : new ArrayList<>(sessions);
        }
    }

    private void handleFailedSession(Session failedSession) {
        Long failedAdminId = (Long) failedSession.getUserProperties().get("adminId");
        AdminOnlineCountVO correctionMessage = removeSession(failedAdminId, failedSession);
        closeSession(failedSession);
        broadcastOnlineCount(correctionMessage);
    }

    private boolean sendTextSafely(Session targetSession, String message) {
        try {
            sendText(targetSession, message);
            return true;
        } catch (IOException | RuntimeException e) {
            log.error("推送消息失败，连接异常，准备移除会话", e);
            return false;
        }
    }

    private void sendText(Session targetSession, String message) throws IOException {
        synchronized (targetSession) {
            if (!targetSession.isOpen()) {
                throw new IOException("WebSocket会话已关闭");
            }
            targetSession.getBasicRemote().sendText(message);
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

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        String[] pairs = query.split("&");
        for (String p : pairs) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }
}

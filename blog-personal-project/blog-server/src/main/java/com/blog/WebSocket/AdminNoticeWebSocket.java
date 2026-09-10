package com.blog.WebSocket;

import com.alibaba.fastjson.JSON;
import com.blog.config.SpringContextHolder;
import com.blog.pojo.dto.SysNoticeDTO;
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
import java.util.stream.Collectors;


@Slf4j
@Component
@ServerEndpoint("/ws/admin/notice")
public class AdminNoticeWebSocket {

    //存放所有WebSocket握手成功的会话对象（同一管理员可能多端登录，所以一个管理员对应多个会话）
    private static final Map<Long, Set<Session>> ONLINE_SESSIONS = new ConcurrentHashMap<>();

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
        ONLINE_SESSIONS.computeIfAbsent(adminId, key -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("【WS】管理员{}连接建立成功，当前在线数量：{}", adminId, getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (adminId != null) {
            removeSession(adminId, session);
        }
        log.info("【WS】管理员{}连接断开，当前在线数量：{}", adminId, getOnlineCount());
    }

    /**
     * 接受前端消息
     * @param msg
     * @param session
     */
    @OnMessage
    public void onMessage(String msg,Session session){
    }

    @OnError
    public void onError(Session session,Throwable throwable){
        log.error("【WS】异常",throwable);
        if (adminId != null) {
            removeSession(adminId, session);
        }
    }

    /**
     * 统计当前在线的管理端数量（同一管理员多端登录分别计数）
     *
     * @return 在线会话数量
     */
    public static long getOnlineCount() {
        return ONLINE_SESSIONS.values().stream().mapToLong(Set::size).sum();
    }

    /**
     * 推送消息
     *
     * @param message
     */
    public void broadcast(SysNoticeDTO message) {
        try {
            //复制一份会话列表，避免遍历过程中集合被修改
            List<Session> sessions = ONLINE_SESSIONS.values().stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toList());
            if (sessions.isEmpty()) {
                log.info("【WS】暂无在线客户端，无需推送");
                return;
            }
            String json;
            try {
                json = JSON.toJSONString(message);
            } catch (Exception e) {
                log.error("【WS】消息序列化为JSON失败", e);
                return;
            }
            for (Session s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        log.error("推送消息失败，连接异常，准备移除会话",e);
                        //发送失败直接剔除僵尸连接
                        Long aid = (Long)s.getUserProperties().get("adminId");
                        if(aid != null){
                            removeSession(aid, s);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 单点推送，发给指定管理员
     */
    public void sendToOne(Long targetAdminId, SysNoticeDTO message){
        Set<Session> sessions = ONLINE_SESSIONS.get(targetAdminId);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("管理员{}不在线，消息无法推送",targetAdminId);
            return;
        }
        try {
            String json = JSON.toJSONString(message);
            //同一管理员可能多端登录，每个会话都要推送
            for (Session session : new ArrayList<>(sessions)) {
                if (!session.isOpen()) {
                    continue;
                }
                session.getBasicRemote().sendText(json);
            }
        } catch (IOException e) {
            log.error("单点推送失败",e);
        }
    }

    /**
     * 移除管理员的指定会话，该管理员没有会话后清理掉对应的key
     *
     * @param adminId 管理员id
     * @param session 需要移除的会话
     */
    private void removeSession(Long adminId, Session session) {
        Set<Session> sessions = ONLINE_SESSIONS.get(adminId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            ONLINE_SESSIONS.remove(adminId);
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

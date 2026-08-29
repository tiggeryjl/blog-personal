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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@ServerEndpoint("/ws/user/notice")
public class userNoticeWebSocket {

    //存放所有WebSocket握手成功的会话对象
    private static final Map<Long, Session> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    private Session session;

    private Long userId;

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
        userId = jwtService.getUserId(token);
        session.getUserProperties().put("userId", userId);
        ONLINE_SESSIONS.put(userId, session);
        log.info("【WS】管理员id:【{}】连接建立成功，当前在线数量：{}", userId, ONLINE_SESSIONS.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(userId != null){
            ONLINE_SESSIONS.remove(userId);
        }
        log.info("【WS】管理员{}连接断开，当前在线数量：{}", userId, ONLINE_SESSIONS.size());
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
        if(userId != null){
            ONLINE_SESSIONS.remove(userId);
        }
    }

    /**
     * 推送消息
     *
     * @param message
     */
    public void broadcast(SysNoticeDTO message) {
        try {
            if (ONLINE_SESSIONS.isEmpty()) {
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
            for (Session s : ONLINE_SESSIONS.values()) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        log.error("推送消息失败，连接异常，准备移除会话",e);
                        //发送失败直接剔除僵尸连接
                        Long aid = (Long)s.getUserProperties().get("adminId");
                        if(aid != null){
                            ONLINE_SESSIONS.remove(aid);
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
        Session session = ONLINE_SESSIONS.get(targetAdminId);
        if(session == null || !session.isOpen()){
            log.warn("管理员{}不在线，消息无法推送",targetAdminId);
            return;
        }
        try {
            String json = JSON.toJSONString(message);
            session.getBasicRemote().sendText(json);
        } catch (IOException e) {
            log.error("单点推送失败",e);
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

package com.blog.WebSocket;

import com.alibaba.fastjson.JSON;
import com.blog.config.SpringContextHolder;
import com.blog.pojo.dto.SysNoticeDTO;
import com.blog.service.JwtService;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;


@Slf4j
@Component
@ServerEndpoint("/ws/admin/notice")
public class AdminNoticeWebSocket {

    //存放所有WebSocket握手成功的会话对象
    private static final CopyOnWriteArraySet<Session> ONLINE_SESSIONS = new CopyOnWriteArraySet<>();

    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        String query = session.getQueryString();
        if(query == null || !query.contains("token=")){
            closeSession(session);
            return;
        }
        Map<String,String> params = parseQuery(query);
        String token = params.get("token");
        if(token == null || token.isBlank()){
            closeSession(session);
            return;
        }

        JwtService jwtService = SpringContextHolder.getBean(JwtService.class);
        if (!jwtService.validateToken(token)) {
            log.warn("【WS】token校验失败，关闭连接");
            closeSession(session);
            return;
        }
        ONLINE_SESSIONS.add(session);
        log.info("【WS】连接建立成功，当前在线数量：{}", ONLINE_SESSIONS.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        ONLINE_SESSIONS.remove(this.session);
        log.info("【WS】连接断开，当前在线数量：{}", ONLINE_SESSIONS.size());
    }

    /**
     * 推送消息
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
            for (Session s : ONLINE_SESSIONS) {
                if (s.isOpen()) {
                    s.getBasicRemote().sendText(json);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭会话
     * @param session
     */
    private void closeSession(Session session){
        try {
            if(session.isOpen()) session.close();
        } catch (Exception ignored) {}
    }

    private Map<String,String> parseQuery(String query){
        Map<String,String> map = new HashMap<>();
        if(query == null) return map;
        String[] pairs = query.split("&");
        for(String p : pairs){
            String[] kv = p.split("=",2);
            if(kv.length==2) map.put(kv[0],kv[1]);
        }
        return map;
    }
}

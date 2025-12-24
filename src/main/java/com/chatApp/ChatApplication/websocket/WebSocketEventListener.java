package com.chatApp.ChatApplication.websocket;

import com.chatApp.ChatApplication.model.UserStatus;
import com.chatApp.ChatApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    // Store session-to-user mapping
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Get userId from session attributes (set during authentication)
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            sessionUserMap.put(sessionId, userId);

            // Update user status to ONLINE
            userService.updateUserStatus(userId, UserStatus.ONLINE);

            // Broadcast user online status - explicit method signature
            String destination = "/topic/user-status";
            Object payload = Map.of("userId", userId, "status", "ONLINE");
            messagingTemplate.convertAndSend(destination, payload);

            log.info("User connected: {} with session: {}", userId, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Long userId = sessionUserMap.remove(sessionId);

        if (userId != null) {
            // Update user status to OFFLINE
            userService.updateUserStatus(userId, UserStatus.OFFLINE);

            // Broadcast user offline status - explicit method signature
            String destination = "/topic/user-status";
            Object payload = Map.of("userId", userId, "status", "OFFLINE");
            messagingTemplate.convertAndSend(destination, payload);

            log.info("User disconnected: {} with session: {}", userId, sessionId);
        }
    }
}
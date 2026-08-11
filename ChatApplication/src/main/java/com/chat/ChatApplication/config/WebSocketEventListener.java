package com.chat.ChatApplication.config;

import com.chat.ChatApplication.dto.UserStatusDto;
import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.service.OnlineUserService;
import com.chat.ChatApplication.service.PresenceDelayService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Handles the WebSocket lifecycle.
 *
 * The offline event is deliberately delayed by a few seconds. Mobile
 * operating systems and browsers can briefly drop/recreate a connection
 * during network changes, app lifecycle changes, or reconnects.
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SessionRegistry sessionRegistry;
    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceDelayService presenceDelayService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        Long userId = sessionRegistry.removeSession(sessionId);

        if (userId == null) {
            return;
        }

        // Cancel/replace any previous delayed offline task for this user.
        presenceDelayService.schedule(userId, () ->
                sessionRegistry.executeIfOffline(userId, () -> {

                    onlineUserService.userDisconnected(userId);

                    User user = onlineUserService.getUser(userId);

                    if (user == null) {
                        return;
                    }

                    messagingTemplate.convertAndSend(
                            "/topic/status",
                            new UserStatusDto(
                                    userId,
                                    false,
                                    user.getLastSeen()
                            )
                    );

                    System.out.println(
                            "🔴 USER OFFLINE : " + userId
                    );
                })
        );
    }
}

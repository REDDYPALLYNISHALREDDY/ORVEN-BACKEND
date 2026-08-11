package com.chat.ChatApplication.config;

import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.event.UserConnectedEvent;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.security.JwtService;
import com.chat.ChatApplication.service.OnlineUserService;
import com.chat.ChatApplication.service.PresenceDelayService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

/**
 * Authenticates STOMP CONNECT using the same JWT used by REST APIs and
 * registers the resulting WebSocket session for presence tracking.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SessionRegistry sessionRegistry;
    private final OnlineUserService onlineUserService;
    private final ApplicationEventPublisher publisher;
    private final PresenceDelayService presenceDelayService;

    @Override
    public Message<?> preSend(
            @NonNull Message<?> message,
            @NonNull MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String auth =
                accessor.getFirstNativeHeader("Authorization");

        if (auth == null || !auth.startsWith("Bearer ")) {
            return message;
        }

        String token = auth.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return message;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            AuthorityUtils.NO_AUTHORITIES
                    );

            accessor.setUser(authentication);

            String sessionId = accessor.getSessionId();

            if (sessionId == null) {
                return message;
            }

            /*
             * addSession() atomically tells us whether this was the first
             * active session for the user.
             */
            boolean becameOnline =
                    sessionRegistry.addSession(sessionId, user.getId());

            /*
             * A reconnect should cancel the pending offline transition.
             */
            presenceDelayService.cancel(user.getId());

            if (becameOnline) {
                onlineUserService.userConnected(user.getId());

                publisher.publishEvent(
                        new UserConnectedEvent(
                                this,
                                user.getId()
                        )
                );

                System.out.println(
                        "🟢 USER ONLINE : " + user.getFullName()
                );
            }

        } catch (Exception e) {
            System.out.println(
                    "❌ WebSocket authentication error: "
                            + e.getMessage()
            );
        }

        return message;
    }
}

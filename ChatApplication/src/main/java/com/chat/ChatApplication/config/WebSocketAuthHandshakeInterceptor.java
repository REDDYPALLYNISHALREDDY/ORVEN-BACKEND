package com.chat.ChatApplication.config;

import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        if (request instanceof ServletServerHttpRequest servletRequest) {

            HttpServletRequest httpRequest =
                    servletRequest.getServletRequest();

            String token =
                    httpRequest.getParameter("token");

            System.out.println("=========== HANDSHAKE ===========");
            System.out.println("TOKEN = " + token);

            if (token != null) {

                try {

                    String email =
                            jwtService.extractEmail(token);

                    User user =
                            userRepository.findByEmail(email)
                                    .orElse(null);

                    if (user != null) {

                        attributes.put("userId", user.getId());

                    }

                } catch (Exception ignored) {

                }

            }

        }

        return true;

    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {

    }

}
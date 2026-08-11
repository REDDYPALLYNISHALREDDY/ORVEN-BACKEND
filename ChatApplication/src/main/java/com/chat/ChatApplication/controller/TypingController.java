package com.chat.ChatApplication.controller;

import com.chat.ChatApplication.dto.TypingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TypingController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/typing")
    public void typing(TypingDto dto) {

        messagingTemplate.convertAndSend(

                "/topic/chat/" +
                        dto.getConversationId() +
                        "/typing",

                dto

        );

    }

}
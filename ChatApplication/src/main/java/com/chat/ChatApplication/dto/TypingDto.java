package com.chat.ChatApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingDto {

    private Long conversationId;

    private Long senderId;

    private String senderName;

    private boolean typing;

}
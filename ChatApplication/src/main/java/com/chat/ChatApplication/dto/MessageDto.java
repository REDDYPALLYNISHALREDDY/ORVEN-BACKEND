package com.chat.ChatApplication.dto;

import com.chat.ChatApplication.entity.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import com.chat.ChatApplication.entity.MessageStatus;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

    private Long id;

    private Long senderId;

    private String senderName;

    private Long conversationId;

    private String content;

    private MessageType type;

    private MessageStatus status;

    private boolean edited;

    private LocalDateTime createdAt;

    private Long replyToId;

    private String replyToContent;

    private String replyToSenderName;

    private Map<String, Integer> reactions;

    private String myReaction;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private boolean starred;
}
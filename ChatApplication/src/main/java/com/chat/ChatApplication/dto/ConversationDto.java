package com.chat.ChatApplication.dto;

import com.chat.ChatApplication.entity.ConversationType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationDto {

    private Long id;

    private ConversationType type;

    private String groupName;

    private String groupImage;

    /* Friend Information */

    private Long userId;

    private String name;

    private String email;

    private String profileImage;

    /* Chat Preview */

    private String lastMessage;

    private String time;

    private Integer unreadCount;

    private boolean online;

    private LocalDateTime lastSeen;

    private boolean archived;

    private List<String> memberNames;

}
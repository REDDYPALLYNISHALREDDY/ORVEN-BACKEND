package com.chat.ChatApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserStatusDto {

    private Long userId;

    private boolean online;

    private LocalDateTime lastSeen;

}
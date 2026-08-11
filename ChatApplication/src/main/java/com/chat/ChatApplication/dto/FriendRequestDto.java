package com.chat.ChatApplication.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendRequestDto {

    private Long id;

    private Long senderId;

    private String senderName;

    private String senderEmail;

    private String senderProfileImage;

    private Long receiverId;

    private String receiverName;

    private String status;

}
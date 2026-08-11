package com.chat.ChatApplication.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationResponse {

    private boolean userExists;

    private String message;

    private Long conversationId;

}
package com.chat.ChatApplication.service;

public interface EmailService {

    void sendInvitationEmail(
            String email,
            String senderName,
            String inviteToken
    );

}
package com.chat.ChatApplication.service;

public interface EmailService {

    void sendInvitationEmail(
            String email,
            String senderName,
            String inviteToken
    );

    void sendVerificationEmail(
            String email,
            String fullName,
            String verificationToken
    );

}
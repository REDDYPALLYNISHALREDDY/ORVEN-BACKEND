package com.chat.ChatApplication.service;

import com.chat.ChatApplication.dto.InviteRequest;
import com.chat.ChatApplication.dto.InvitationResponse;

public interface InvitationService {

    InvitationResponse inviteMember(
            InviteRequest request,
            String loggedInEmail
    );

}
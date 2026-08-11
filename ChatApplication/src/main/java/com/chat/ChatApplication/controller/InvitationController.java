package com.chat.ChatApplication.controller;

import com.chat.ChatApplication.dto.ApiResponse;
import com.chat.ChatApplication.dto.InviteRequest;
import com.chat.ChatApplication.dto.InvitationResponse;
import com.chat.ChatApplication.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    public ApiResponse<InvitationResponse> invite(

            @RequestBody InviteRequest request,

            Authentication authentication

    ) {

        return new ApiResponse<>(

                true,

                "Invitation processed successfully.",

                invitationService.inviteMember(

                        request,

                        authentication.getName()

                )

        );

    }



}
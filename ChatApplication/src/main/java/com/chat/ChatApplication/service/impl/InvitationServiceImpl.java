package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.InviteRequest;
import com.chat.ChatApplication.dto.InvitationResponse;
import com.chat.ChatApplication.entity.*;
import com.chat.ChatApplication.exception.ResourceNotFoundException;
import com.chat.ChatApplication.repository.InvitationRepository;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.chat.ChatApplication.service.FriendRequestService;
import com.chat.ChatApplication.service.EmailService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;

    private final UserRepository userRepository;

    private final FriendRequestService friendRequestService;

    private final EmailService emailService;

    private User currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

    }

    @Override
    public InvitationResponse inviteMember(
            InviteRequest request,
            String loggedInEmail
    ) {

        User sender = currentUser();

        User receiver = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (receiver != null) {

            friendRequestService.sendRequest(
                    receiver.getId()
            );

            return InvitationResponse.builder()
                    .userExists(true)
                    .message("Friend request sent successfully.")
                    .build();

        }

        String inviteToken = UUID.randomUUID().toString();

        Invitation invitation = Invitation.builder()
                .email(request.getEmail())
                .inviteToken(inviteToken)
                .sender(sender)
                .status(InvitationStatus.PENDING)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        invitationRepository.save(invitation);

        // Send Email
        if (request.getEmail() != null &&
                !request.getEmail().isBlank()) {

            emailService.sendInvitationEmail(

                    request.getEmail(),

                    sender.getFullName(),

                    inviteToken

            );

        }
        return InvitationResponse.builder()
                .userExists(false)
                .message("Invitation saved successfully.")
                .build();

    }

}
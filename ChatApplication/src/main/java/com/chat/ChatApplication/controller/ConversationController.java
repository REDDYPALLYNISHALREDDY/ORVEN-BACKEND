package com.chat.ChatApplication.controller;

import com.chat.ChatApplication.dto.ApiResponse;
import com.chat.ChatApplication.dto.ConversationDto;
import com.chat.ChatApplication.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.chat.ChatApplication.dto.CreateGroupRequest;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/private/{friendId}")
    public ApiResponse<ConversationDto> createPrivateConversation(
            @PathVariable Long friendId) {

        return new ApiResponse<>(
                true,
                "Conversation created successfully",
                conversationService.createPrivateConversation(friendId)
        );
    }

    @GetMapping
    public ApiResponse<List<ConversationDto>> myConversations() {

        return new ApiResponse<>(
                true,
                "Conversations fetched successfully",
                conversationService.myConversations()
        );
    }

    @PostMapping("/group")
    public ApiResponse<ConversationDto> createGroup(

            @RequestBody CreateGroupRequest request

    ) {

        return new ApiResponse<>(

                true,

                "Group created successfully",

                conversationService.createGroup(request)

        );

    }

    @DeleteMapping("/{conversationId}")
    public ApiResponse<Void> deleteConversation(
            @PathVariable Long conversationId) {

        conversationService.deleteConversation(conversationId);

        return new ApiResponse<>(
                true,
                "Conversation deleted",
                null
        );
    }

    @PutMapping("/{conversationId}/archive")
    public ApiResponse<Void> archiveConversation(
            @PathVariable Long conversationId) {

        conversationService.archiveConversation(conversationId);

        return new ApiResponse<>(
                true,
                "Conversation archived",
                null
        );

    }

    @PutMapping("/{conversationId}/unarchive")
    public ApiResponse<Void> unarchiveConversation(
            @PathVariable Long conversationId) {

        conversationService.unarchiveConversation(conversationId);

        return new ApiResponse<>(
                true,
                "Conversation restored",
                null
        );

    }
}
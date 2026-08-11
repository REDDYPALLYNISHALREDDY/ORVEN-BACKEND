package com.chat.ChatApplication.controller;

import com.chat.ChatApplication.dto.ApiResponse;
import com.chat.ChatApplication.dto.MessageDto;
import com.chat.ChatApplication.dto.SendMessageRequest;
import com.chat.ChatApplication.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/{conversationId}")
    public ApiResponse<MessageDto> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {

        return new ApiResponse<>(
                true,
                "Message sent successfully",
                messageService.sendMessage(conversationId, request)
        );
    }

    @PostMapping("/{conversationId}/file")
    public ApiResponse<MessageDto> sendFile(

            @PathVariable Long conversationId,

            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "content", required = false)

                    String content

    ) {

        return new ApiResponse<>(

                true,

                "File sent successfully",

                messageService.sendFile(

                        conversationId,

                        file,

                        content

                )

        );

    }

    @GetMapping("/{conversationId}")
    public ApiResponse<List<MessageDto>> getMessages(
            @PathVariable Long conversationId) {

        return new ApiResponse<>(
                true,
                "Messages fetched successfully",
                messageService.getMessages(conversationId)
        );
    }

    @PutMapping("/{conversationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long conversationId
    ) {

        messageService.markAsRead(conversationId);

        return new ApiResponse<>(
                true,
                "Messages marked as read",
                null
        );

    }

    @PutMapping("/{messageId}/star")
    public ApiResponse<MessageDto> toggleStar(
            @PathVariable Long messageId) {

        return new ApiResponse<>(

                true,

                "Message updated",

                messageService.toggleStar(messageId)

        );

    }

    @DeleteMapping("/conversation/{conversationId}")
    public ApiResponse<Void> clearChat(
            @PathVariable Long conversationId) {

        messageService.clearChat(conversationId);

        return new ApiResponse<>(

                true,

                "Chat cleared successfully",

                null

        );

    }
}
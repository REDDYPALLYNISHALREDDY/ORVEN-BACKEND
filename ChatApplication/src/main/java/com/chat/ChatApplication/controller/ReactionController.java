package com.chat.ChatApplication.controller;

import com.chat.ChatApplication.dto.MessageDto;
import com.chat.ChatApplication.dto.ReactionRequest;
import com.chat.ChatApplication.service.ReactionService;
import com.chat.ChatApplication.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageDto>> reactToMessage(
            @PathVariable Long messageId,
            @RequestBody ReactionRequest request
    ) {

        MessageDto dto = reactionService.reactToMessage(messageId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reaction updated successfully",
                        dto
                )
        );
    }

}
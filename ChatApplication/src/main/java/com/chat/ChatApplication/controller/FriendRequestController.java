package com.chat.ChatApplication.controller;

import com.chat.ChatApplication.dto.ApiResponse;
import com.chat.ChatApplication.dto.FriendRequestDto;
import com.chat.ChatApplication.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.chat.ChatApplication.dto.FriendDto;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping("/send/{receiverId}")
    public ApiResponse<FriendRequestDto> sendRequest(
            @PathVariable Long receiverId) {

        return new ApiResponse<>(
                true,
                "Friend request sent successfully",
                friendRequestService.sendRequest(receiverId)
        );
    }

    @PutMapping("/accept/{requestId}")
    public ApiResponse<FriendRequestDto> acceptRequest(
            @PathVariable Long requestId) {

        return new ApiResponse<>(
                true,
                "Friend request accepted",
                friendRequestService.acceptRequest(requestId)
        );
    }

    @PutMapping("/reject/{requestId}")
    public ApiResponse<FriendRequestDto> rejectRequest(
            @PathVariable Long requestId) {

        return new ApiResponse<>(
                true,
                "Friend request rejected",
                friendRequestService.rejectRequest(requestId)
        );
    }

    @GetMapping("/pending")
    public ApiResponse<List<FriendRequestDto>> pendingRequests() {

        return new ApiResponse<>(
                true,
                "Pending requests fetched successfully",
                friendRequestService.pendingRequests()
        );
    }

    @GetMapping
    public ApiResponse<List<FriendDto>> friends() {

        return new ApiResponse<>(
                true,
                "Friends fetched successfully",
                friendRequestService.getFriends()
        );
    }
}
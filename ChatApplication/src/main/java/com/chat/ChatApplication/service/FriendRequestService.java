package com.chat.ChatApplication.service;

import com.chat.ChatApplication.dto.FriendRequestDto;
import com.chat.ChatApplication.dto.FriendDto;

import java.util.List;

public interface FriendRequestService {

    FriendRequestDto sendRequest(
            Long receiverId
    );

    FriendRequestDto acceptRequest(
            Long requestId
    );

    FriendRequestDto rejectRequest(
            Long requestId
    );

    List<FriendRequestDto> pendingRequests();

    List<FriendDto> getFriends();

    String getStatus(
            Long userId
    );
}
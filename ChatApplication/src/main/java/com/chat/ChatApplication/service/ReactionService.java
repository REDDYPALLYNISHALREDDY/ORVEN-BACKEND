package com.chat.ChatApplication.service;

import com.chat.ChatApplication.dto.MessageDto;
import com.chat.ChatApplication.dto.ReactionRequest;

public interface ReactionService {

    MessageDto reactToMessage(
            Long messageId,
            ReactionRequest request
    );

}
package com.chat.ChatApplication.service;

import com.chat.ChatApplication.dto.ConversationDto;
import com.chat.ChatApplication.dto.CreateGroupRequest;

import java.util.List;

public interface ConversationService {

    ConversationDto createPrivateConversation(Long friendId);

    List<ConversationDto> myConversations();

    ConversationDto createGroup(CreateGroupRequest request);

    void deleteConversation(Long conversationId);

    void archiveConversation(Long conversationId);

    void unarchiveConversation(Long conversationId);

}
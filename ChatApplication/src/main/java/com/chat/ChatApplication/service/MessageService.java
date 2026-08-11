package com.chat.ChatApplication.service;

import com.chat.ChatApplication.dto.MessageDto;
import com.chat.ChatApplication.dto.SendMessageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MessageService {

    MessageDto sendMessage(Long conversationId,
                           SendMessageRequest request);

    List<MessageDto> getMessages(Long conversationId);

    void markAsRead(Long conversationId);

    MessageDto sendFile(

            Long conversationId,

            MultipartFile file,

            String content

    );

    MessageDto toggleStar(Long messageId);

    void clearChat(Long conversationId);



}
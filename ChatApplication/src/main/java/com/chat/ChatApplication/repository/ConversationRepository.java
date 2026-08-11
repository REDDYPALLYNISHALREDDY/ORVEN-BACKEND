package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.Conversation;
import com.chat.ChatApplication.entity.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    List<Conversation> findByType(ConversationType type);

}
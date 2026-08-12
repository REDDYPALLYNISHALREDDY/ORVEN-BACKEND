package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.Conversation;
import com.chat.ChatApplication.entity.ConversationMember;
import com.chat.ChatApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ConversationMemberRepository
        extends JpaRepository<ConversationMember, Long> {

    List<ConversationMember> findByUser(
            User user
    );

    List<ConversationMember> findByConversation(
            Conversation conversation
    );

    List<ConversationMember> findByConversationId(
            Long conversationId
    );

    @Transactional
    void deleteByConversation(
            Conversation conversation
    );

    @Transactional
    void deleteByUser(
            User user
    );
}
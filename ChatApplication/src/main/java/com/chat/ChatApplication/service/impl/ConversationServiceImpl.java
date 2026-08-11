package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.ConversationDto;
import com.chat.ChatApplication.entity.*;
import com.chat.ChatApplication.exception.ResourceNotFoundException;
import com.chat.ChatApplication.repository.ConversationMemberRepository;
import com.chat.ChatApplication.repository.ConversationRepository;
import com.chat.ChatApplication.repository.FriendshipRepository;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.repository.MessageRepository;
import com.chat.ChatApplication.service.ConversationService;
import com.chat.ChatApplication.dto.CreateGroupRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl
        implements ConversationService {

    private final ConversationRepository conversationRepository;

    private final ConversationMemberRepository memberRepository;

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    private final MessageRepository messageRepository;


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User currentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }


    // =========================================================
    // FORMAT CHAT TIME
    // =========================================================

    private String formatMessageTime(
            LocalDateTime dateTime
    ) {

        if (dateTime == null) {
            return "";
        }

        LocalDateTime now =
                LocalDateTime.now();


        // -----------------------------------------
        // TODAY
        // Example: 9:00 PM
        // -----------------------------------------

        if (dateTime.toLocalDate()
                .equals(now.toLocalDate())) {

            return dateTime.format(
                    DateTimeFormatter.ofPattern(
                            "h:mm a"
                    )
            );
        }


        // -----------------------------------------
        // YESTERDAY
        // -----------------------------------------

        if (dateTime.toLocalDate()
                .equals(
                        now.toLocalDate()
                                .minusDays(1)
                )) {

            return "Yesterday";
        }


        // -----------------------------------------
        // OLDER
        // Example: 10 Aug
        // -----------------------------------------

        return dateTime.format(
                DateTimeFormatter.ofPattern(
                        "dd MMM"
                )
        );
    }


    // =========================================================
    // FIND EXISTING PRIVATE CONVERSATION
    // =========================================================

    private Conversation findExistingPrivateConversation(
            User me,
            User friend
    ) {

        List<Conversation> conversations =
                conversationRepository.findByType(
                        ConversationType.PRIVATE
                );


        for (Conversation conversation :
                conversations) {

            List<ConversationMember> members =
                    memberRepository
                            .findByConversation(
                                    conversation
                            );


            boolean hasMe =
                    members.stream()
                            .anyMatch(
                                    member ->
                                            member.getUser()
                                                    .getId()
                                                    .equals(
                                                            me.getId()
                                                    )
                            );


            boolean hasFriend =
                    members.stream()
                            .anyMatch(
                                    member ->
                                            member.getUser()
                                                    .getId()
                                                    .equals(
                                                            friend.getId()
                                                    )
                            );


            if (hasMe &&
                    hasFriend &&
                    members.size() == 2) {

                return conversation;
            }
        }


        return null;
    }


    // =========================================================
    // MAP CONVERSATION TO DTO
    // =========================================================

    private ConversationDto map(
            Conversation conversation
    ) {

        User me = currentUser();


        // =====================================================
        // GROUP CHAT
        // =====================================================

        if (conversation.getType()
                == ConversationType.GROUP) {

            List<ConversationMember> members =
                    memberRepository
                            .findByConversation(
                                    conversation
                            );


            List<String> memberNames =
                    members.stream()
                            .map(
                                    member ->
                                            member.getUser()
                                                    .getFullName()
                            )
                            .toList();


            Message lastMessage =
                    messageRepository
                            .findTopByConversationOrderByCreatedAtDesc(
                                    conversation
                            )
                            .orElse(null);


            String preview;


            if (lastMessage == null) {

                preview =
                        "You created this group";

            } else if (
                    lastMessage.getType()
                            == MessageType.FILE
            ) {

                preview =
                        "📄 "
                                + lastMessage.getFileName();

            } else if (
                    lastMessage.getType()
                            == MessageType.IMAGE
            ) {

                preview =
                        "🖼 Photo";

            } else {

                preview =
                        lastMessage.getContent();
            }


            // Use last message time.
            // If there is no message yet,
            // use conversation creation time.

            LocalDateTime timeToDisplay =
                    lastMessage != null
                            ? lastMessage.getCreatedAt()
                            : conversation.getCreatedAt();


            return ConversationDto.builder()

                    .id(conversation.getId())

                    .type(conversation.getType())

                    .groupName(
                            conversation.getGroupName()
                    )

                    .groupImage(
                            conversation.getGroupImage()
                    )

                    .name(
                            conversation.getGroupName()
                    )

                    .memberNames(memberNames)

                    .lastMessage(preview)

                    .time(
                            formatMessageTime(
                                    timeToDisplay
                            )
                    )

                    .unreadCount(0)

                    .build();
        }


        // =====================================================
        // PRIVATE CHAT
        // =====================================================

        List<ConversationMember> members =
                memberRepository
                        .findByConversation(
                                conversation
                        );


        User otherUser =
                members.stream()

                        .map(
                                ConversationMember::getUser
                        )

                        .filter(
                                user ->
                                        !user.getId()
                                                .equals(
                                                        me.getId()
                                                )
                        )

                        .findFirst()

                        .orElse(me);


        Message lastMessage =
                messageRepository
                        .findTopByConversationOrderByCreatedAtDesc(
                                conversation
                        )
                        .orElse(null);


        String preview;


        if (lastMessage == null) {

            preview =
                    "No messages yet";

        } else if (
                lastMessage.getType()
                        == MessageType.FILE
        ) {

            preview =
                    "📄 "
                            + lastMessage.getFileName();

        } else if (
                lastMessage.getType()
                        == MessageType.IMAGE
        ) {

            preview =
                    "🖼 Photo";

        } else {

            preview =
                    lastMessage.getContent();
        }


        // Use last message time.
        // If there is no message yet,
        // use conversation creation time.

        LocalDateTime timeToDisplay =
                lastMessage != null
                        ? lastMessage.getCreatedAt()
                        : conversation.getCreatedAt();


        return ConversationDto.builder()

                .id(conversation.getId())

                .type(conversation.getType())

                .userId(
                        otherUser.getId()
                )

                .groupName(
                        conversation.getGroupName()
                )

                .groupImage(
                        conversation.getGroupImage()
                )

                .name(
                        otherUser.getFullName()
                )

                .email(
                        otherUser.getEmail()
                )

                .profileImage(
                        otherUser.getProfileImage()
                )

                .lastMessage(preview)

                .time(
                        formatMessageTime(
                                timeToDisplay
                        )
                )

                .online(
                        otherUser.isOnline()
                )

                .lastSeen(
                        otherUser.getLastSeen()
                )

                .archived(
                        conversation.isArchived()
                )

                .unreadCount(0)

                .build();
    }


    // =========================================================
    // CREATE PRIVATE CONVERSATION
    // =========================================================

    @Override
    public ConversationDto createPrivateConversation(
            Long friendId
    ) {

        User me = currentUser();


        User friend =
                userRepository
                        .findById(friendId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Friend not found"
                                        )
                        );


        boolean isFriend =
                friendshipRepository
                        .findByUser1AndUser2(
                                me,
                                friend
                        )
                        .isPresent()

                        ||

                        friendshipRepository
                                .findByUser1AndUser2(
                                        friend,
                                        me
                                )
                                .isPresent();


        if (!isFriend) {

            throw new RuntimeException(
                    "You are not friends."
            );
        }


        Conversation existingConversation =
                findExistingPrivateConversation(
                        me,
                        friend
                );


        if (existingConversation != null) {

            return map(
                    existingConversation
            );
        }


        Conversation conversation =
                Conversation.builder()

                        .type(
                                ConversationType.PRIVATE
                        )

                        .build();


        conversationRepository.save(
                conversation
        );


        memberRepository.save(

                ConversationMember.builder()

                        .conversation(
                                conversation
                        )

                        .user(me)

                        .build()
        );


        memberRepository.save(

                ConversationMember.builder()

                        .conversation(
                                conversation
                        )

                        .user(friend)

                        .build()
        );


        return map(
                conversation
        );
    }


    // =========================================================
    // MY CONVERSATIONS
    // =========================================================

    @Override
    public List<ConversationDto> myConversations() {

        User me = currentUser();


        return memberRepository

                .findByUser(me)

                .stream()

                .map(
                        member ->
                                map(
                                        member.getConversation()
                                )
                )

                .toList();
    }


    // =========================================================
    // CREATE GROUP
    // =========================================================

    @Override
    public ConversationDto createGroup(
            CreateGroupRequest request
    ) {

        User me = currentUser();


        Conversation conversation =
                Conversation.builder()

                        .type(
                                ConversationType.GROUP
                        )

                        .groupName(
                                request.getGroupName()
                        )

                        .build();


        conversationRepository.save(
                conversation
        );


        // Creator

        memberRepository.save(

                ConversationMember.builder()

                        .conversation(
                                conversation
                        )

                        .user(me)

                        .build()
        );


        // Members

        for (Long memberId :
                request.getMemberIds()) {

            User member =
                    userRepository
                            .findById(memberId)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "User not found"
                                            )
                            );


            memberRepository.save(

                    ConversationMember.builder()

                            .conversation(
                                    conversation
                            )

                            .user(member)

                            .build()
            );
        }


        return ConversationDto.builder()

                .id(
                        conversation.getId()
                )

                .type(
                        conversation.getType()
                )

                .groupName(
                        conversation.getGroupName()
                )

                .groupImage(
                        conversation.getGroupImage()
                )

                .lastMessage("")

                .time(
                        formatMessageTime(
                                conversation.getCreatedAt()
                        )
                )

                .unreadCount(0)

                .build();
    }


    // =========================================================
    // DELETE CONVERSATION
    // =========================================================

    @Override
    @Transactional
    public void deleteConversation(
            Long conversationId
    ) {

        User me = currentUser();


        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Conversation not found"
                                        )
                        );


        boolean isMember =
                memberRepository
                        .findByConversation(
                                conversation
                        )

                        .stream()

                        .anyMatch(
                                member ->
                                        member.getUser()
                                                .getId()
                                                .equals(
                                                        me.getId()
                                                )
                        );


        if (!isMember) {

            throw new RuntimeException(
                    "Access denied."
            );
        }


        messageRepository
                .deleteByConversation(
                        conversation
                );


        memberRepository
                .deleteByConversation(
                        conversation
                );


        conversationRepository
                .delete(
                        conversation
                );
    }


    // =========================================================
    // ARCHIVE
    // =========================================================

    @Override
    @Transactional
    public void archiveConversation(
            Long conversationId
    ) {

        User me = currentUser();


        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Conversation not found"
                                        )
                        );


        boolean isMember =
                memberRepository
                        .findByConversation(
                                conversation
                        )
                        .stream()
                        .anyMatch(
                                conversationMember ->
                                        conversationMember
                                                .getUser()
                                                .getId()
                                                .equals(
                                                        me.getId()
                                                )
                        );


        if (!isMember) {

            throw new RuntimeException(
                    "Access denied."
            );
        }


        conversation.setArchived(
                true
        );


        conversationRepository.save(
                conversation
        );
    }


    // =========================================================
    // UNARCHIVE
    // =========================================================

    @Override
    @Transactional
    public void unarchiveConversation(
            Long conversationId
    ) {

        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Conversation not found"
                                        )
                        );


        conversation.setArchived(
                false
        );


        conversationRepository.save(
                conversation
        );
    }
}
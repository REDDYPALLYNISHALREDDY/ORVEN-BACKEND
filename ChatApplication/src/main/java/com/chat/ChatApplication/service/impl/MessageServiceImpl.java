package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.MessageDto;
import com.chat.ChatApplication.dto.SendMessageRequest;
import com.chat.ChatApplication.entity.Conversation;
import com.chat.ChatApplication.entity.ConversationMember;
import com.chat.ChatApplication.entity.Message;
import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.exception.ResourceNotFoundException;
import com.chat.ChatApplication.repository.ConversationMemberRepository;
import com.chat.ChatApplication.repository.ConversationRepository;
import com.chat.ChatApplication.repository.MessageRepository;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.service.MessageService;
import com.chat.ChatApplication.entity.MessageStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import org.springframework.web.multipart.MultipartFile;
import com.chat.ChatApplication.service.FileUploadService;
import com.chat.ChatApplication.entity.MessageType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileUploadService fileUploadService;

    private User currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private MessageDto map(Message message) {

        MessageDto dto = MessageDto.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .starred(message.isStarred())
                .conversationId(message.getConversation().getId())
                .content(message.getContent())
                .fileName(message.getFileName())

                .fileUrl(message.getFileUrl())

                .fileType(message.getFileType())

                .fileSize(message.getFileSize())
                .type(message.getType())
                .edited(message.isEdited())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();

        if (message.getReplyTo() != null) {

            dto.setReplyToId(
                    message.getReplyTo().getId()
            );

            dto.setReplyToContent(
                    message.getReplyTo().getContent()
            );

            dto.setReplyToSenderName(
                    message.getReplyTo()
                            .getSender()
                            .getFullName()
            );

        }

        return dto;

    }

    @Override
    public MessageDto sendMessage(Long conversationId,
                                  SendMessageRequest request) {

        User me = currentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found"));

        boolean isMember = memberRepository.findByConversation(conversation)
                .stream()
                .map(ConversationMember::getUser)
                .anyMatch(user -> user.getId().equals(me.getId()));

        if (!isMember) {
            throw new RuntimeException("You are not a member of this conversation.");
        }

        Message replyMessage = null;

        if (request.getReplyToId() != null) {

            replyMessage = messageRepository
                    .findById(request.getReplyToId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Reply message not found"));

        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(me)
                .content(request.getContent())
                .replyTo(replyMessage)
                .status(MessageStatus.SENT)
                .build();

        Message savedMessage = messageRepository.save(message);

        MessageDto dto = map(savedMessage);

        System.out.println("========== DTO ==========");
        System.out.println("SenderId      : " + dto.getSenderId());
        System.out.println("SenderName    : " + dto.getSenderName());
        System.out.println("ConversationId: " + dto.getConversationId());
        System.out.println("Content       : " + dto.getContent());
        System.out.println("=========================");
        messagingTemplate.convertAndSend(
                "/topic/chat/" + conversationId,
                dto
        );

        return dto;
    }

    @Override
    public List<MessageDto> getMessages(Long conversationId) {

        User me = currentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found"));

        boolean isMember = memberRepository.findByConversation(conversation)
                .stream()
                .map(ConversationMember::getUser)
                .anyMatch(user -> user.getId().equals(me.getId()));

        if (!isMember) {
            throw new RuntimeException("Access denied.");
        }

        List<Message> messages =
                messageRepository.findByConversationOrderByCreatedAtAsc(conversation);

// Mark received messages as DELIVERED
        messages.stream()
                .filter(message ->
                        !message.getSender().getId().equals(me.getId()))
                .filter(message ->
                        message.getStatus() == MessageStatus.SENT)
                .forEach(message -> {
                    message.setStatus(MessageStatus.DELIVERED);
                    messageRepository.save(message);
                });

        return messages.stream()
                .map(this::map)
                .toList();
    }

    @Override
    public void markAsRead(Long conversationId) {

        User me = currentUser();

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conversation not found"
                                )
                        );

        List<Message> unreadMessages =
                messageRepository
                        .findByConversationAndSenderNotAndStatus(
                                conversation,
                                me,
                                MessageStatus.SENT
                        );

        List<Message> deliveredMessages =
                messageRepository
                        .findByConversationAndSenderNotAndStatus(
                                conversation,
                                me,
                                MessageStatus.DELIVERED
                        );

        /*
         * Combine SENT + DELIVERED messages.
         *
         * This is important because a message received
         * through WebSocket can still be SENT in the
         * database when the receiver is already inside
         * the chat.
         */

        List<Message> messagesToRead =
                new java.util.ArrayList<>();

        messagesToRead.addAll(
                unreadMessages
        );

        deliveredMessages.forEach(
                message -> {

                    if (
                            messagesToRead.stream()
                                    .noneMatch(
                                            existing ->
                                                    existing.getId()
                                                            .equals(
                                                                    message.getId()
                                                            )
                                    )
                    ) {

                        messagesToRead.add(
                                message
                        );

                    }

                }
        );


        /*
         * Change all received messages to READ.
         */

        messagesToRead.forEach(
                message ->
                        message.setStatus(
                                MessageStatus.READ
                        )
        );


        /*
         * Save database changes first.
         */

        messageRepository.saveAll(
                messagesToRead
        );


        /*
         * Broadcast READ status immediately.
         *
         * The sender's WebSocket subscription will
         * receive this updated MessageDto and change
         * the tick to BLUE without refresh.
         */

        for (
                Message message :
                messagesToRead
        ) {

            MessageDto dto =
                    map(message);

            messagingTemplate.convertAndSend(
                    "/topic/chat/" +
                            conversationId,
                    dto
            );

        }

    }

    @Override
    public MessageDto sendFile(
            Long conversationId,
            MultipartFile file,
            String content
    ) {

        System.out.println("Name : " + file.getOriginalFilename());
        System.out.println("Type : " + file.getContentType());
        System.out.println("Size : " + file.getSize());

        User me = currentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found"));

        String fileUrl;

        try {

            fileUrl = fileUploadService.uploadFile(file);

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Cloudinary Upload Error : " + e.getMessage(),
                    e
            );

        }

        Message message = Message.builder()

                .conversation(conversation)

                .sender(me)

                .content(content == null ? "" : content)

                .fileName(file.getOriginalFilename())

                .fileUrl(fileUrl)

                .fileType(file.getContentType())

                .fileSize(file.getSize())

                .type(

                        file.getContentType().startsWith("image/")

                                ? MessageType.IMAGE

                                : MessageType.FILE

                )

                .status(MessageStatus.SENT)

                .build();

        Message saved = messageRepository.save(message);

        MessageDto dto = map(saved);

        messagingTemplate.convertAndSend(

                "/topic/chat/" + conversationId,

                dto

        );

        return dto;

    }

    @Override
    public MessageDto toggleStar(Long messageId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Message not found"));

        message.setStarred(!message.isStarred());

        messageRepository.save(message);

        return map(message);

    }

    @Override
    @Transactional
    public void clearChat(Long conversationId) {

        User me = currentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found"));

        boolean isMember = memberRepository.findByConversation(conversation)
                .stream()
                .map(ConversationMember::getUser)
                .anyMatch(user -> user.getId().equals(me.getId()));

        if (!isMember) {

            throw new RuntimeException("Access denied.");

        }

        messageRepository.deleteByConversation(conversation);

    }
}
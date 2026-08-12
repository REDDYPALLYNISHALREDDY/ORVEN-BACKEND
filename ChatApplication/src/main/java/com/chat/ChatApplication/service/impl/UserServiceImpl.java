package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.UserResponse;
import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.service.UserService;
import com.chat.ChatApplication.dto.UpdateProfileRequest;
import com.chat.ChatApplication.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.chat.ChatApplication.dto.ChangePasswordRequest;
import org.springframework.transaction.annotation.Transactional;
import com.chat.ChatApplication.dto.PrivacySettingsDto;

import com.chat.ChatApplication.dto.ProfileStatsDto;
import com.chat.ChatApplication.entity.ConversationMember;
import com.chat.ChatApplication.entity.ConversationType;
import com.chat.ChatApplication.repository.ConversationMemberRepository;
import com.chat.ChatApplication.repository.MessageRepository;
import com.chat.ChatApplication.repository.FriendshipRepository;
import com.chat.ChatApplication.dto.SharedMediaDto;

import org.springframework.web.multipart.MultipartFile;
import com.chat.ChatApplication.service.CloudinaryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final CloudinaryService cloudinaryService;

    private final ConversationMemberRepository conversationMemberRepository;

    private final FriendshipRepository friendshipRepository;

    private final MessageRepository messageRepository;

    @Override
    public UserResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = repository.findByEmail(authentication.getName())
                .orElseThrow();

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .emailVerified(user.isEmailVerified())
                .build();

    }

    private UserResponse map(User user){

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .emailVerified(user.isEmailVerified())
                .role(user.getRole())
                .build();

    }

    @Override
    public UserResponse updateProfile(UpdateProfileRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = repository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());

        user.setBio(request.getBio());

        if (request.getProfileImage() == null ||
                request.getProfileImage().isBlank()) {

            user.setProfileImage(null);

        } else {

            user.setProfileImage(request.getProfileImage());

        }

        repository.save(user);

        return map(user);
    }

    @Override
    public UserResponse getUserById(Long id){

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return map(user);

    }

    @Override
    public List<UserResponse> getAllUsers(){

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();

    }

    @Override
    public List<UserResponse> searchUsers(String keyword){

        return repository.findByFullNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::map)
                .toList();

    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = repository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException("Current password is incorrect.");

        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException("Passwords do not match.");

        }

        user.setPassword(

                passwordEncoder.encode(
                        request.getNewPassword()
                )

        );

        repository.save(user);

    }

    @Override
    public UserResponse uploadProfileImage(MultipartFile file) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = repository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        String imageUrl = cloudinaryService.uploadImage(file);

        user.setProfileImage(imageUrl);

        repository.save(user);

        return map(user);

    }

    @Override
    public ProfileStatsDto getProfileStats() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = repository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Friends
        long friends =
                friendshipRepository.findByUser1(user).size()
                        + friendshipRepository.findByUser2(user).size();

        // Conversations
        var memberships = conversationMemberRepository.findByUser(user);

        long conversations = memberships.size();

        long groups = memberships.stream()
                .filter(member ->
                        member.getConversation().getType() == ConversationType.GROUP)
                .count();

        // Files Shared
        long filesShared =
                messageRepository.countBySenderAndFileUrlIsNotNull(user);

        return ProfileStatsDto.builder()
                .conversations(conversations)
                .friends(friends)
                .groups(groups)
                .filesShared(filesShared)
                .build();

    }
    @Override
    public List<SharedMediaDto> getSharedMedia() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = repository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return messageRepository

                .findBySenderAndFileUrlIsNotNull(user)

                .stream()

                .map(message ->

                        SharedMediaDto.builder()

                                .id(message.getId())

                                .fileName(message.getFileName())

                                .fileUrl(message.getFileUrl())

                                .fileType(message.getFileType())

                                .build()

                )

                .toList();

    }

    @Override
    public PrivacySettingsDto getPrivacySettings() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = repository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return PrivacySettingsDto.builder()
                .showOnlineStatus(user.isShowOnlineStatus())
                .showLastSeen(user.isShowLastSeen())
                .readReceipts(user.isReadReceipts())
                .build();

    }

    @Override
    public PrivacySettingsDto updatePrivacySettings(
            PrivacySettingsDto request
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = repository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setShowOnlineStatus(request.isShowOnlineStatus());

        user.setShowLastSeen(request.isShowLastSeen());

        user.setReadReceipts(request.isReadReceipts());

        repository.save(user);

        return PrivacySettingsDto.builder()
                .showOnlineStatus(user.isShowOnlineStatus())
                .showLastSeen(user.isShowLastSeen())
                .readReceipts(user.isReadReceipts())
                .build();

    }

}
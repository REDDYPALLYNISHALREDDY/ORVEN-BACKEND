package com.chat.ChatApplication.service;

import com.chat.ChatApplication.dto.UpdateProfileRequest;
import com.chat.ChatApplication.dto.UserResponse;
import com.chat.ChatApplication.dto.ChangePasswordRequest;
import org.springframework.web.multipart.MultipartFile;
import com.chat.ChatApplication.dto.ProfileStatsDto;
import com.chat.ChatApplication.dto.SharedMediaDto;
import com.chat.ChatApplication.dto.PrivacySettingsDto;

import java.util.List;

public interface UserService {

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateProfileRequest request);

    UserResponse uploadProfileImage(MultipartFile file);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    List<UserResponse> searchUsers(String keyword);

    void changePassword(ChangePasswordRequest request);

    ProfileStatsDto getProfileStats();

    List<SharedMediaDto> getSharedMedia();

    PrivacySettingsDto getPrivacySettings();

    PrivacySettingsDto updatePrivacySettings(
            PrivacySettingsDto request
    );

}
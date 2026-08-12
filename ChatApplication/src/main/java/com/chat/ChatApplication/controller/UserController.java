package com.chat.ChatApplication.controller;

import com.chat.ChatApplication.dto.ApiResponse;
import com.chat.ChatApplication.dto.UpdateProfileRequest;
import com.chat.ChatApplication.dto.UserResponse;
import com.chat.ChatApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import com.chat.ChatApplication.dto.ProfileStatsDto;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import com.chat.ChatApplication.dto.PrivacySettingsDto;

import jakarta.validation.Valid;

import java.util.List;

import java.util.Set;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.chat.ChatApplication.dto.ChangePasswordRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.chat.ChatApplication.dto.SharedMediaDto;

import com.chat.ChatApplication.service.OnlineUserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final OnlineUserService OnlineUserService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {

        return new ApiResponse<>(
                true,
                "User fetched successfully",
                userService.getCurrentUser()
        );

    }

    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(
            @RequestBody UpdateProfileRequest request){

        return new ApiResponse<>(
                true,
                "Profile updated successfully",
                userService.updateProfile(request)
        );

    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers(){

        return new ApiResponse<>(
                true,
                "Users fetched successfully",
                userService.getAllUsers()
        );

    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(
            @PathVariable Long id){

        return new ApiResponse<>(
                true,
                "User fetched successfully",
                userService.getUserById(id)
        );

    }

    @GetMapping("/search")
    public ApiResponse<List<UserResponse>> searchUsers(
            @RequestParam String keyword){

        return new ApiResponse<>(
                true,
                "Users fetched successfully",
                userService.searchUsers(keyword)
        );

    }

    @GetMapping("/online")
    public Set<Long> online(){

        return OnlineUserService.getOnlineUsers();

    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(

            @RequestBody @Valid ChangePasswordRequest request

    ) {

        userService.changePassword(request);

        return new ApiResponse<>(

                true,

                "Password changed successfully",

                null

        );

    }

    @PostMapping("/profile-image")
    public ApiResponse<UserResponse> uploadProfileImage(

            @RequestPart("file") MultipartFile file

    ) {

        return new ApiResponse<>(

                true,

                "Profile image uploaded successfully.",

                userService.uploadProfileImage(file)

        );

    }

    @GetMapping("/profile-stats")
    public ApiResponse<ProfileStatsDto> getProfileStats() {

        return new ApiResponse<>(

                true,

                "Profile statistics fetched successfully.",

                userService.getProfileStats()

        );

    }

    @GetMapping("/shared-media")
    public ApiResponse<List<SharedMediaDto>> getSharedMedia() {

        return new ApiResponse<>(

                true,

                "Shared media fetched successfully.",

                userService.getSharedMedia()

        );

    }

    @GetMapping("/privacy")
    public ApiResponse<PrivacySettingsDto> getPrivacySettings() {

        return new ApiResponse<>(

                true,

                "Privacy settings fetched.",

                userService.getPrivacySettings()

        );

    }

    @PutMapping("/privacy")
    public ApiResponse<PrivacySettingsDto> updatePrivacySettings(

            @RequestBody PrivacySettingsDto request

    ) {

        return new ApiResponse<>(

                true,

                "Privacy settings updated.",

                userService.updatePrivacySettings(request)

        );

    }


    @DeleteMapping("/delete-account")
    public ApiResponse<Void> deleteAccount() {

        userService.deleteAccount();

        return new ApiResponse<>(
                true,
                "Account deleted successfully",
                null
        );
    }

}
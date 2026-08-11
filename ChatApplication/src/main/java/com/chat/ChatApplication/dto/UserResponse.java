package com.chat.ChatApplication.dto;

import com.chat.ChatApplication.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private Role role;

    private String profileImage;

    private String bio;

}
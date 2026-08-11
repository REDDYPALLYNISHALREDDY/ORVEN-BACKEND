package com.chat.ChatApplication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {

    private String email;

    private String fullName;

    private String photoUrl;

}
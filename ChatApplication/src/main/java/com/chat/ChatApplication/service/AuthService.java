package com.chat.ChatApplication.service;

import com.chat.ChatApplication.dto.GoogleLoginRequest;
import com.chat.ChatApplication.dto.LoginRequest;
import com.chat.ChatApplication.dto.RegisterRequest;
import com.chat.ChatApplication.dto.GoogleTokenRequest;

public interface AuthService {

    String register(RegisterRequest request);

    String login(LoginRequest request);

    String googleLogin(GoogleTokenRequest request);

}
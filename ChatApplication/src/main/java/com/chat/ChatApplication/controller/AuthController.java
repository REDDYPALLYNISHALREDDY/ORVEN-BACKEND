package com.chat.ChatApplication.controller;
import com.chat.ChatApplication.dto.GoogleTokenRequest;

import com.chat.ChatApplication.dto.ApiResponse;
import com.chat.ChatApplication.dto.LoginRequest;
import com.chat.ChatApplication.dto.RegisterRequest;
import com.chat.ChatApplication.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<String> register(
            @Valid @RequestBody RegisterRequest request) {

        String token = authService.register(request);

        return new ApiResponse<>(
                true,
                "User registered successfully",
                token
        );
    }

    @PostMapping("/login")
    public ApiResponse<String> login(
            @Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        return new ApiResponse<>(
                true,
                "Login successful",
                token
        );
    }

    @PostMapping("/google")
    public ApiResponse<String> googleLogin(

            @RequestBody GoogleTokenRequest request

    ) {

        String token = authService.googleLogin(request);

        return new ApiResponse<>(

                true,

                "Google Login Successful",

                token

        );

    }

}
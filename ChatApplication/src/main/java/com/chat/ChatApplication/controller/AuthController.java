package com.chat.ChatApplication.controller;

import com.chat.ChatApplication.dto.ApiResponse;
import com.chat.ChatApplication.dto.GoogleTokenRequest;
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


    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ApiResponse<String> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        authService.register(request);

        return new ApiResponse<>(
                true,
                "Registration successful. Please check your email and verify your account.",
                null
        );
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ApiResponse<String> login(
            @Valid @RequestBody LoginRequest request
    ) {

        String token =
                authService.login(request);

        return new ApiResponse<>(
                true,
                "Login successful",
                token
        );
    }


    // =========================================================
    // GOOGLE LOGIN
    // =========================================================

    @PostMapping("/google")
    public ApiResponse<String> googleLogin(
            @RequestBody GoogleTokenRequest request
    ) {

        String token =
                authService.googleLogin(request);

        return new ApiResponse<>(
                true,
                "Google Login Successful",
                token
        );
    }


    // =========================================================
    // VERIFY EMAIL
    // =========================================================

    @GetMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(
            @RequestParam String token
    ) {

        authService.verifyEmail(token);

        return new ApiResponse<>(
                true,
                "Email verified successfully. You can now log in.",
                null
        );
    }
}
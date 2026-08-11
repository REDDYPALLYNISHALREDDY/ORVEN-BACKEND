package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.LoginRequest;
import com.chat.ChatApplication.dto.RegisterRequest;
import com.chat.ChatApplication.entity.Role;
import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.exception.ResourceAlreadyExistsException;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.security.JwtService;
import com.chat.ChatApplication.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.chat.ChatApplication.dto.GoogleTokenRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public String register(RegisterRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        repository.save(user);

        return jwtService.generateToken(user.getEmail());
    }

    @Override
    public String login(LoginRequest request) {

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return jwtService.generateToken(user.getEmail());
    }

    @Override
    public String googleLogin(GoogleTokenRequest request) {

        try {

            FirebaseToken decodedToken =
                    FirebaseAuth.getInstance()
                            .verifyIdToken(request.getToken());

            String email = decodedToken.getEmail();

            String name = decodedToken.getName();

            User user = repository
                    .findByEmail(email)
                    .orElse(null);

            if (user == null) {

                user = User.builder()
                        .email(email)
                        .fullName(name)
                        .password("")
                        .role(Role.USER)
                        .build();

                repository.save(user);

            }

            return jwtService.generateToken(user.getEmail());

        } catch (Exception e) {

            throw new RuntimeException("Invalid Google Token");

        }

    }

}
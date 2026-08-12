package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.dto.GoogleTokenRequest;
import com.chat.ChatApplication.dto.LoginRequest;
import com.chat.ChatApplication.dto.RegisterRequest;
import com.chat.ChatApplication.entity.Role;
import com.chat.ChatApplication.entity.User;
import com.chat.ChatApplication.exception.ResourceAlreadyExistsException;
import com.chat.ChatApplication.repository.UserRepository;
import com.chat.ChatApplication.security.JwtService;
import com.chat.ChatApplication.service.AuthService;
import com.chat.ChatApplication.service.EmailService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;


    // =========================================================
    // NORMAL EMAIL/PASSWORD REGISTRATION
    // =========================================================

    @Override
    public String register(RegisterRequest request) {

        // Check whether email already exists
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Email already exists"
            );
        }


        // Generate a secure random verification token
        String verificationToken =
                UUID.randomUUID().toString();


        // Create user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(Role.USER)

                // Email verification
                .emailVerified(false)
                .emailVerificationToken(
                        verificationToken
                )
                .emailVerificationTokenExpiry(
                        LocalDateTime.now()
                                .plusHours(24)
                )

                .build();


        // Save user
        repository.save(user);


        // Send verification email
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                verificationToken
        );


        /*
         * IMPORTANT:
         *
         * Do NOT generate a JWT here.
         *
         * User must verify the email first.
         */
        return null;
    }


    // =========================================================
    // NORMAL EMAIL/PASSWORD LOGIN
    // =========================================================

    @Override
    public String login(LoginRequest request) {

        // Find user
        User user = repository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );


        // Check email verification BEFORE login
        if (!user.isEmailVerified()) {

            throw new RuntimeException(
                    "Please verify your email before logging in."
            );
        }


        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }


        // Generate JWT only after verification
        return jwtService.generateToken(
                user.getEmail()
        );
    }


    // =========================================================
    // GOOGLE LOGIN
    // =========================================================

    @Override
    public String googleLogin(
            GoogleTokenRequest request
    ) {

        try {

            // Verify Firebase Google ID token
            FirebaseToken decodedToken =
                    FirebaseAuth.getInstance()
                            .verifyIdToken(
                                    request.getToken()
                            );


            String email =
                    decodedToken.getEmail();

            String name =
                    decodedToken.getName();


            // Make sure Google provided an email
            if (email == null ||
                    email.trim().isEmpty()) {

                throw new RuntimeException(
                        "Google account email not available"
                );
            }


            // Find existing Orven user
            User user = repository
                    .findByEmail(email)
                    .orElse(null);


            // =================================================
            // NEW GOOGLE USER
            // =================================================

            if (user == null) {

                // Fallback name if Google doesn't provide one
                if (name == null ||
                        name.trim().isEmpty()) {

                    name = email.split("@")[0];
                }


                user = User.builder()
                        .email(email)
                        .fullName(name)

                        /*
                         * Google/Firebase has already
                         * authenticated the email.
                         *
                         * Therefore no separate
                         * email verification is required.
                         */
                        .emailVerified(true)

                        .password("")

                        .role(Role.USER)

                        .build();


                repository.save(user);

            }


            // =================================================
            // EXISTING USER LOGGING IN WITH GOOGLE
            // =================================================

            else {

                /*
                 * If this user was previously created with
                 * email/password but is now authenticating
                 * successfully through Google, Google has
                 * verified the account identity.
                 *
                 * Therefore mark the email as verified.
                 */

                if (!user.isEmailVerified()) {

                    user.setEmailVerified(true);

                    user.setEmailVerificationToken(null);

                    user.setEmailVerificationTokenExpiry(
                            null
                    );

                    repository.save(user);
                }
            }


            // =================================================
            // GENERATE ORVEN JWT
            // =================================================

            return jwtService.generateToken(
                    user.getEmail()
            );


        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid Google Token"
            );
        }
    }


    // =========================================================
    // VERIFY EMAIL
    // =========================================================

    public void verifyEmail(String token) {

        // Find user using verification token
        User user = repository
                .findByEmailVerificationToken(token)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid verification link."
                        )
                );


        // Already verified
        if (user.isEmailVerified()) {

            throw new RuntimeException(
                    "Email is already verified."
            );
        }


        // Check token expiry
        if (user.getEmailVerificationTokenExpiry() == null ||
                user.getEmailVerificationTokenExpiry()
                        .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Verification link has expired."
            );
        }


        // Mark email as verified
        user.setEmailVerified(true);


        // Remove token so it cannot be reused
        user.setEmailVerificationToken(null);

        user.setEmailVerificationTokenExpiry(null);


        // Save changes
        repository.save(user);
    }
}
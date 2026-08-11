package com.chat.ChatApplication.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {

        try {

            if (FirebaseApp.getApps().isEmpty()) {

                String firebaseConfig =
                        System.getenv("FIREBASE_CONFIG");

                System.out.println("Firebase Config Present: " + (firebaseConfig != null));
                System.out.println("Firebase Config Length: " +
                        (firebaseConfig != null ? firebaseConfig.length() : 0));

                FirebaseOptions options =
                        FirebaseOptions.builder()
                                .setCredentials(
                                        GoogleCredentials.fromStream(
                                                new ByteArrayInputStream(
                                                        firebaseConfig.getBytes(StandardCharsets.UTF_8)
                                                )
                                        )
                                )
                                .build();

                FirebaseApp.initializeApp(options);

                System.out.println("Firebase Initialized");

            }

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

}
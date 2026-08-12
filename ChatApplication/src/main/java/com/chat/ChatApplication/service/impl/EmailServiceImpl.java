package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String senderEmail;

    @Value("${BREVO_SENDER_NAME:Orven}")
    private String senderName;

    private final RestTemplate restTemplate =
            new RestTemplate();


    // =========================================================
    // INVITATION EMAIL
    // =========================================================

    @Override
    public void sendInvitationEmail(
            String email,
            String inviterName,
            String inviteToken
    ) {

        try {

            // Invitation URL
            String inviteUrl =
                    "https://orvenchat.vercel.app/register?invite="
                            + inviteToken;


            // =================================================
            // EMAIL HTML
            // =================================================

            String html = """
                    <!DOCTYPE html>
                    <html>

                    <head>
                        <meta charset="UTF-8">

                        <meta
                            name="viewport"
                            content="width=device-width, initial-scale=1.0"
                        >
                    </head>

                    <body style="
                        margin:0;
                        padding:40px 20px;
                        background:#0f172a;
                        font-family:Arial,sans-serif;
                    ">

                        <div style="
                            max-width:600px;
                            margin:auto;
                            background:#1e293b;
                            border-radius:18px;
                            padding:40px 30px;
                            color:white;
                            text-align:center;
                        ">

                            <h1 style="
                                color:#38bdf8;
                                font-size:40px;
                                margin:0 0 10px 0;
                            ">
                                Orven
                            </h1>

                            <h2 style="
                                color:#ffffff;
                                font-size:28px;
                                margin:15px 0 20px 0;
                            ">
                                You're Invited! ⚡
                            </h2>

                            <p style="
                                font-size:17px;
                                line-height:1.7;
                                color:#e2e8f0;
                            ">
                                <strong>%s</strong>
                                invited you to join Orven.
                            </p>

                            <p style="
                                font-size:15px;
                                line-height:1.7;
                                color:#94a3b8;
                            ">
                                Chat with friends, react to messages,
                                share files and connect in real-time.
                            </p>

                            <a
                                href="%s"
                                style="
                                    display:inline-block;
                                    margin-top:25px;
                                    padding:15px 30px;
                                    background:#2563eb;
                                    color:#ffffff;
                                    text-decoration:none;
                                    border-radius:10px;
                                    font-size:16px;
                                    font-weight:bold;
                                "
                            >
                                Join Orven
                            </a>

                            <p style="
                                margin-top:40px;
                                font-size:13px;
                                color:#64748b;
                            ">
                                This invitation was sent from Orven.
                            </p>

                        </div>

                    </body>
                    </html>
                    """
                    .formatted(
                            inviterName,
                            inviteUrl
                    );


            // =================================================
            // BREVO REQUEST BODY
            // =================================================

            Map<String, Object> sender =
                    new HashMap<>();

            sender.put(
                    "name",
                    senderName
            );

            sender.put(
                    "email",
                    this.senderEmail
            );


            Map<String, Object> recipient =
                    new HashMap<>();

            recipient.put(
                    "email",
                    email
            );


            Map<String, Object> requestBody =
                    new HashMap<>();

            requestBody.put(
                    "sender",
                    sender
            );

            requestBody.put(
                    "to",
                    List.of(recipient)
            );

            requestBody.put(
                    "subject",
                    "You're Invited to Orven ⚡"
            );

            requestBody.put(
                    "htmlContent",
                    html
            );


            // =================================================
            // HEADERS
            // =================================================

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            headers.set(
                    "api-key",
                    brevoApiKey
            );

            headers.set(
                    "accept",
                    "application/json"
            );


            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            requestBody,
                            headers
                    );


            // =================================================
            // SEND EMAIL
            // =================================================

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            "https://api.brevo.com/v3/smtp/email",
                            request,
                            String.class
                    );


            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "✅ Invitation email sent successfully!"
            );

            System.out.println(
                    "Recipient: " + email
            );

            System.out.println(
                    "Brevo Response: "
                            + response.getBody()
            );

            System.out.println(
                    "======================================"
            );

        } catch (Exception e) {

            System.err.println(
                    "======================================"
            );

            System.err.println(
                    "❌ Invitation email failed!"
            );

            System.err.println(
                    "Recipient: " + email
            );

            System.err.println(
                    "Error: " + e.getMessage()
            );

            System.err.println(
                    "======================================"
            );

            throw new RuntimeException(
                    "Email sending failed: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // =========================================================
    // EMAIL VERIFICATION
    // =========================================================

    @Override
    public void sendVerificationEmail(
            String email,
            String fullName,
            String verificationToken
    ) {

        try {

            // =================================================
            // VERIFICATION URL
            // =================================================

            String verificationUrl =
                    "https://orvenchat.vercel.app/verify-email?token="
                            + verificationToken;


            // =================================================
            // EMAIL HTML
            // =================================================

            String html = """
                    <!DOCTYPE html>
                    <html>

                    <head>
                        <meta charset="UTF-8">

                        <meta
                            name="viewport"
                            content="width=device-width, initial-scale=1.0"
                        >
                    </head>

                    <body style="
                        margin:0;
                        padding:40px 20px;
                        background:#020617;
                        font-family:Arial,sans-serif;
                    ">

                        <div style="
                            max-width:600px;
                            margin:auto;
                            background:#0f172a;
                            border:1px solid #1e293b;
                            border-radius:20px;
                            padding:40px 30px;
                            color:white;
                            text-align:center;
                        ">

                            <h1 style="
                                color:#38bdf8;
                                font-size:40px;
                                margin:0 0 10px 0;
                            ">
                                Orven
                            </h1>

                            <h2 style="
                                color:#ffffff;
                                font-size:28px;
                                margin:15px 0 20px 0;
                            ">
                                Verify your email
                            </h2>

                            <p style="
                                color:#cbd5e1;
                                font-size:16px;
                                line-height:1.7;
                            ">
                                Hello <strong>%s</strong>,
                            </p>

                            <p style="
                                color:#cbd5e1;
                                font-size:16px;
                                line-height:1.7;
                            ">
                                Thank you for creating your
                                Orven account.
                            </p>

                            <p style="
                                color:#cbd5e1;
                                font-size:16px;
                                line-height:1.7;
                            ">
                                Please verify your email address
                                to activate your account.
                            </p>

                            <a
                                href="%s"
                                style="
                                    display:inline-block;
                                    margin-top:25px;
                                    padding:15px 32px;
                                    background:#2563eb;
                                    color:#ffffff;
                                    text-decoration:none;
                                    border-radius:10px;
                                    font-size:16px;
                                    font-weight:bold;
                                "
                            >
                                Verify Email
                            </a>

                            <p style="
                                margin-top:35px;
                                color:#64748b;
                                font-size:13px;
                                line-height:1.6;
                            ">
                                This verification link expires
                                in 24 hours.
                            </p>

                            <p style="
                                color:#64748b;
                                font-size:12px;
                                line-height:1.6;
                            ">
                                If you did not create this account,
                                you can safely ignore this email.
                            </p>

                        </div>

                    </body>
                    </html>
                    """
                    .formatted(
                            fullName,
                            verificationUrl
                    );


            // =================================================
            // BREVO SENDER
            // =================================================

            Map<String, Object> sender =
                    new HashMap<>();

            sender.put(
                    "name",
                    senderName
            );

            sender.put(
                    "email",
                    this.senderEmail
            );


            // =================================================
            // BREVO RECIPIENT
            // =================================================

            Map<String, Object> recipient =
                    new HashMap<>();

            recipient.put(
                    "email",
                    email
            );


            // =================================================
            // BREVO REQUEST BODY
            // =================================================

            Map<String, Object> requestBody =
                    new HashMap<>();

            requestBody.put(
                    "sender",
                    sender
            );

            requestBody.put(
                    "to",
                    List.of(recipient)
            );

            requestBody.put(
                    "subject",
                    "Verify your Orven account ⚡"
            );

            requestBody.put(
                    "htmlContent",
                    html
            );


            // =================================================
            // HEADERS
            // =================================================

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            headers.set(
                    "api-key",
                    brevoApiKey
            );

            headers.set(
                    "accept",
                    "application/json"
            );


            // =================================================
            // REQUEST
            // =================================================

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            requestBody,
                            headers
                    );


            // =================================================
            // SEND VERIFICATION EMAIL
            // =================================================

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            "https://api.brevo.com/v3/smtp/email",
                            request,
                            String.class
                    );


            // =================================================
            // LOG
            // =================================================

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "✅ Verification email sent successfully!"
            );

            System.out.println(
                    "Recipient: " + email
            );

            System.out.println(
                    "Brevo Response: "
                            + response.getBody()
            );

            System.out.println(
                    "======================================"
            );

        } catch (Exception e) {

            System.err.println(
                    "======================================"
            );

            System.err.println(
                    "❌ Verification email failed!"
            );

            System.err.println(
                    "Recipient: " + email
            );

            System.err.println(
                    "Error: " + e.getMessage()
            );

            System.err.println(
                    "======================================"
            );

            throw new RuntimeException(
                    "Verification email sending failed: "
                            + e.getMessage(),
                    e
            );
        }
    }
}
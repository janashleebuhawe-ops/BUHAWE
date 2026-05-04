package com.ordering.system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:janashleebuhawe@gmail.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:BUHAWE}")
    private String senderName;

    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/users/verify?token=" + token;

        String subject = "Verify your LMS account";
        String body =
            "Hello!\n\n" +
            "An account was created for you on the LMS system.\n\n" +
            "Please click the link below to verify your email and activate your account:\n\n" +
            link + "\n\n" +
            "If you did not expect this email, you can safely ignore it.\n\n" +
            "— Nexus Identity";

        if (brevoApiKey != null && !brevoApiKey.isEmpty()) {
            // Railway: send via Brevo REST API (HTTPS - never blocked)
            sendViaBrevo(toEmail, subject, body);
        } else {
            // Local: send via Gmail SMTP
            sendViaSmtp(toEmail, subject, body);
        }
    }

    private void sendViaSmtp(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private void sendViaBrevo(String toEmail, String subject, String body) {
        RestTemplate restTemplate = new RestTemplate();

        String json = """
            {
                "sender": { "name": "%s", "email": "%s" },
                "to": [{ "email": "%s" }],
                "subject": "%s",
                "textContent": "%s"
            }
            """.formatted(
                senderName,
                senderEmail,
                toEmail,
                subject,
                body.replace("\n", "\\n").replace("\"", "\\\"")
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        try {
            restTemplate.postForEntity(
                "https://api.brevo.com/v3/smtp/email",
                request,
                String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Brevo email failed: " + e.getMessage(), e);
        }
    }
}
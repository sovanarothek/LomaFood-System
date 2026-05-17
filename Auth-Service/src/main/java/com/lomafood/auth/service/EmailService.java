package com.lomafood.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("LomaFood - Email Verification OTP");
        message.setText(
            "Hello,\n\n" +
            "Your OTP code is: " + otp + "\n\n" +
            "This code expires in 5 minutes.\n\n" +
            "LomaFood Team"
        );
        mailSender.send(message);
    }

    public void sendPasswordResetLink(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("LomaFood - Password Reset Request");
        message.setText(
            "Hello,\n\n" +
            "Click the link below to reset your password:\n\n" +
            resetLink + "\n\n" +
            "This link expires in 15 minutes.\n\n" +
            "LomaFood Team"
        );
        mailSender.send(message);
    }
}

package com.tracemydata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your email";
        String verificationLink = frontendUrl + "/verify?token=" + token;
        String body = "Click the link  below to verify your email: \n" + verificationLink + "\n\n" +
              "This link will expire in 24 hours.\n\n" +
              "Thanks,\n TraceMyData Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(fromEmail);
        mailSender.send(message);
    }

    public void sendForgotPassword(String to, String token) {
        String subject = "Forgot Password  email";
        String verificationLink = frontendUrl + "/reset-password?token=" + token;
        String body = "Hi,\n\nWe received a request to reset your password. " +
              "Click the link below to set a new password. This link will expire in 30 minutes.\n\n" +
              verificationLink + "\n\n" +
              "If you didn't request a password reset, please ignore this email.\n\n" +
              "Thanks,\n TraceMyData Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(fromEmail);
        mailSender.send(message);
    }

   
    
}

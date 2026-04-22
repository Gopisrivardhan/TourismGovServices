package com.tourismgov.notification.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async 
    public void sendNotificationEmail(String to, String name, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            // YOUR COLORFUL HTML TEMPLATE PRESERVED
            String htmlMsg = "<div style='background-color: #f0f2f5; padding: 40px 10px; font-family: \"Segoe UI\", Helvetica, Arial, sans-serif;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 24px rgba(0,0,0,0.12);'>"
                + "<div style='background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); padding: 30px; text-align: center;'>"
                + "<div style='color: #ffffff; font-size: 28px; font-weight: 700;'>TOURISM GOV INDIA</div></div>"
                + "<div style='padding: 40px; color: #333333;'>"
                + "<h2>Important Account Update</h2><p>Hello <strong>" + name + "</strong>,</p>"
                + "<div style='background-color: #f8fbff; padding: 20px; border-left: 5px solid #2a5298;'>" + body + "</div>"
                + "</div></div></div>";

            helper.setText(htmlMsg, true); 
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("omkarchoramale05@gmail.com", "Tourism Gov Portal");

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
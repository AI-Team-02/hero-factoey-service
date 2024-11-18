package com.herofactory.subscription.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.properties.company-name}")
    private String companyName;

    @Async
    @Retryable(
            value = {MailException.class, MessagingException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Void> sendEmail(String recipientEmail, String subject, String content) {
        return CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = emailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(String.format("%s <%s>", companyName, senderEmail));
                helper.setTo(recipientEmail);
                helper.setSubject(subject);
                helper.setText(getEmailTemplate(content), true);

                emailSender.send(message);
                log.info("Email sent successfully to: {}", recipientEmail);

            } catch (MailException e) {
                log.error("Failed to send email to: {}, reason: {}", recipientEmail, e.getMessage());
                throw e;
            } catch (MessagingException e) {
                log.error("Error creating email message for: {}, reason: {}", recipientEmail, e.getMessage());
                throw new RuntimeException("Failed to create email message", e);
            }
        });
    }

    private String getEmailTemplate(String content) {
        return String.format("""
            <html>
            <body>
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px;">
                        <h2 style="color: #333;">%s</h2>
                        <div style="margin: 20px 0;">%s</div>
                        <div style="margin-top: 30px; font-size: 12px; color: #666;">
                            본 메일은 발신 전용입니다. 문의사항은 고객센터를 이용해 주세요.<br>
                            © %s All rights reserved.
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, companyName, content, companyName);
    }
}
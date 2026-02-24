package com.pm.notificationservice.service.channel.impl;

import com.pm.notificationservice.service.channel.EmailSender;
import com.pm.notificationservice.templates.EmailTemplate;
import com.pm.notificationservice.templates.EmailTemplateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;

/**
 * Email sender implementation using JavaMailSender (SMTP)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JavaMailEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final EmailTemplateFactory templateFactory;

    @Value("${notification.from-email}")
    private String fromEmail;

    @Value("${notification.reply-to-email:}")
    private String replyToEmail;

    @Override
    public boolean send(List<String> recipients, String subject, String htmlContent, String plainText) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("No recipients provided for email");
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(plainText, htmlContent);

            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                helper.setReplyTo(replyToEmail);
            }

            mailSender.send(message);
            log.info("Email sent successfully to {} recipients for subject: {}", recipients.size(), subject);
            return true;

        } catch (MessagingException e) {
            log.error("Failed to send email to recipients", e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while sending email", e);
            return false;
        }
    }

    @Override
    public boolean sendWithTemplate(List<String> recipients, String templateName, Map<String, Object> data) {
        try {
            EmailTemplate template = templateFactory.getTemplate(templateName);
            String htmlContent = template.buildContent(data);
            String plainText = template.buildPlainText(data);
            String subject = template.getSubject();

            return send(recipients, subject, htmlContent, plainText);

        } catch (Exception e) {
            log.error("Failed to send templated email", e);
            return false;
        }
    }
}


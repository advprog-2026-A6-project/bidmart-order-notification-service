package id.ac.ui.cs.advprog.ordernotification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(java.util.Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender.orElse(null);
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Email to {} was not sent. Content: {}", to, text);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

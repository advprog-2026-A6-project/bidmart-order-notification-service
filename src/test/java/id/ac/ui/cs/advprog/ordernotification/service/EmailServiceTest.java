package id.ac.ui.cs.advprog.ordernotification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConstructorWithMailSender() {
        emailService = new EmailService(Optional.of(mailSender));
        assertNotNull(emailService);
    }

    @Test
    void testConstructorWithEmptyOptional() {
        emailService = new EmailService(Optional.empty());
        assertNotNull(emailService);
    }

    @Test
    void testSendSimpleEmail_Success() {
        emailService = new EmailService(Optional.of(mailSender));
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Content";

        emailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(text, sentMessage.getText());
    }

    @Test
    void testSendSimpleEmail_MailSenderNull() {
        emailService = new EmailService(Optional.empty());

        assertDoesNotThrow(() -> emailService.sendSimpleEmail("test@example.com", "Subject", "Content"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendSimpleEmail_Exception() {
        emailService = new EmailService(Optional.of(mailSender));
        doThrow(new RuntimeException("SMTP Error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendSimpleEmail("test@example.com", "Subject", "Content"));

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}

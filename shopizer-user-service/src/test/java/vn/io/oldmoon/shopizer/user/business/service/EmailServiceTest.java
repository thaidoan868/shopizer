package vn.io.oldmoon.shopizer.user.business.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  @InjectMocks private EmailService emailService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailService, "fromEmail", "test@shopizer.io");
    ReflectionTestUtils.setField(emailService, "fromDisplayName", "Shopizer Team");
  }

  @Test
  @DisplayName("sendMail should construct MimeMessage and send email successfully")
  void sendMail_WhenValidInput_ShouldSendEmail() {
    MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    emailService.sendMail("recipient@example.com", "Subject", "Hello Content");

    verify(mailSender).send(mimeMessage);
  }

  @Test
  @DisplayName("sendMail should throw ServiceException when mailSender throws exception")
  void sendMail_WhenMailSenderFails_ShouldThrowServiceException() {
    MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

    assertThatThrownBy(
            () -> emailService.sendMail("recipient@example.com", "Subject", "Hello Content"))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining("Failed to send email");
  }
}

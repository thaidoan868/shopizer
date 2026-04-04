package vn.io.oldmoon.shopizer.user.business.service;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${spring.mail.from-display-name}")
  private String fromDisplayName;

  public void sendMail(String toEmail, String subject, String content) {
    try {
      // create email body
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper =
          new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
      helper.setTo(toEmail);
      helper.setFrom(fromEmail, fromDisplayName);
      helper.setSubject(subject);
      helper.setText(content, false);

      mailSender.send(mimeMessage);
    } catch (Exception e) {
      throw new ServiceException("Failed to send email", e);
    }
    log.info("Sent email from {} to {} with subject {}", fromEmail, toEmail, subject);
  }
}

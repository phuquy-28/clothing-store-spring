package com.example.clothingstore.service;

import com.example.clothingstore.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

  private final Logger log = LoggerFactory.getLogger(EmailService.class);
  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  public EmailService(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
    this.javaMailSender = javaMailSender;
    this.templateEngine = templateEngine;
  }

  public void sendEmailSync(String to, String subject, String content, boolean isMultipart,
      boolean isHtml) {
    // Prepare message using a Spring helper
    MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
    try {
      MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart,
          StandardCharsets.UTF_8.name());
      message.setTo(to);
      message.setSubject(subject);
      message.setText(content, isHtml);
      this.javaMailSender.send(mimeMessage);
    } catch (MailException | MessagingException e) {
      System.out.println("ERROR SEND EMAIL: " + e);
    }
  }

  public void sendEmailFromTemplateSync(String to, String subject, String templateName,
      String username, String key) {

    Context context = new Context();
    context.setVariable("name", username);
    context.setVariable("activationKey", key);

    String content = templateEngine.process(templateName, context);
    this.sendEmailSync(to, subject, content, false, true);
  }

  @Async
  public void sendActivationEmail(User user) {
    log.debug("Sending activation email to '{}'", user.getEmail());
    this.sendEmailFromTemplateSync(user.getEmail(), "[MINIMOG] Activate your account",
        "mail/activationEmail", user.getEmail(), user.getActivationKey());
  }

}

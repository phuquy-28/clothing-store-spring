package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {

  private final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

  private final JavaMailSender javaMailSender;

  private final SpringTemplateEngine templateEngine;

  @Value("${cors.allowed-origins}")
  private String baseUrl;

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
      log.error("Email could not be sent to user '{}'", to, e);
    }
  }

  public void sendEmailFromTemplateSync(String to, String subject, String templateName,
      String username, String key) {

    Context context = new Context();
    context.setVariable("name", username);
    context.setVariable("key", key);
    context.setVariable("baseUrl", baseUrl);

    String content = templateEngine.process(templateName, context);
    this.sendEmailSync(to, subject, content, false, true);
  }

  @Async
  public void sendActivationEmail(User user) {
    log.debug("Sending activation email to '{}'", user.getEmail());
    this.sendEmailFromTemplateSync(user.getEmail(), AppConstant.ACTIVATION_EMAIL_SUBJECT,
        AppConstant.ACTIVATION_EMAIL_TEMPLATE, user.getEmail(), user.getActivationKey());
  }

  @Async
  public void sendRecoverPasswordEmail(User user) {
    log.debug("Sending recover password email to '{}'", user.getEmail());
    this.sendEmailFromTemplateSync(user.getEmail(), AppConstant.RECOVER_PASSWORD_EMAIL_SUBJECT,
        AppConstant.RECOVER_PASSWORD_EMAIL_TEMPLATE, user.getEmail(), user.getResetKey());
  }
}

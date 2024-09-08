package com.example.clothingstore.service;

import com.example.clothingstore.entity.User;

public interface EmailService {

  public void sendEmailSync(String to, String subject, String content, boolean isMultipart,
      boolean isHtml);

  public void sendEmailFromTemplateSync(String to, String subject, String templateName,
      String username, String key);

  public void sendActivationEmail(User user);

  public void sendRecoverPasswordEmail(User user);
}

package com.example.clothingstore.service;

import java.util.List;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.User;

public interface EmailService {

  public void sendEmailSync(String to, String subject, String content, boolean isMultipart,
      boolean isHtml);

  public void sendEmailFromTemplateSync(String to, String subject, String templateName,
      String username, String key);

  public void sendActivationEmail(User user);

  public void sendRecoverPasswordEmail(User user);

  public void sendOrderConfirmationEmail(Order order, List<ProductVariant> productVariants);

  public void sendActivationCodeEmail(User user);

  public void sendResetCodeEmail(User user);

  public void sendProfileOtpMobile(User user);

  public void sendNewUserAccountEmail(User user, String rawPassword);
}

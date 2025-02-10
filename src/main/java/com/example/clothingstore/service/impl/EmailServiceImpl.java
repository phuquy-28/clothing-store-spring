package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Order.ShippingInformation;
import com.example.clothingstore.entity.ProductImage;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
      MimeMessageHelper message =
          new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
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
  @Override
  public void sendActivationEmail(User user) {
    log.debug("Sending activation email to '{}'", user.getEmail());
    String username = user.getProfile().getFirstName() != null ? user.getProfile().getFirstName()
        : user.getEmail();
    this.sendEmailFromTemplateSync(user.getEmail(), AppConstant.ACTIVATION_EMAIL_SUBJECT,
        AppConstant.ACTIVATION_EMAIL_TEMPLATE, username, user.getActivationKey());
  }

  @Async
  @Override
  public void sendRecoverPasswordEmail(User user) {
    log.debug("Sending recover password email to '{}'", user.getEmail());
    String username = user.getProfile().getFirstName() != null ? user.getProfile().getFirstName()
        : user.getEmail();
    this.sendEmailFromTemplateSync(user.getEmail(), AppConstant.RECOVER_PASSWORD_EMAIL_SUBJECT,
        AppConstant.RECOVER_PASSWORD_EMAIL_TEMPLATE, username, user.getResetKey());
  }

  @Async
  @Transactional(readOnly = true)
  @Override
  public void sendOrderConfirmationEmail(Order order, List<ProductVariant> productVariants) {
    log.debug("Sending order confirmation email to '{}'", order.getUser().getEmail());

    // Tạo map để lookup variant images nhanh hơn
    Map<Long, ProductVariant> variantImageMap = productVariants.stream()
        .collect(Collectors.toMap(ProductVariant::getId, variant -> variant));

    Context context = new Context();
    
    // Format ngày đặt hàng
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    String formattedDate = formatter.format(order.getOrderDate());
    
    // Order information
    context.setVariable("orderNumber", order.getCode());
    context.setVariable("orderDate", formattedDate);
    
    // Customer information
    String customerName = order.getUser().getProfile().getFirstName() != null ? 
        order.getUser().getProfile().getFirstName() : 
        order.getUser().getEmail();
    context.setVariable("customerName", customerName);
    
    // Tính toán các loại tiền
    context.setVariable("subtotal", String.format("%,.0f₫", order.getTotal()));
    context.setVariable("shippingFee", String.format("%,.0f₫", order.getShippingFee()));
    context.setVariable("discount", String.format("%,.0f₫", order.getDiscount()));
    context.setVariable("orderTotal", String.format("%,.0f₫", order.getFinalTotal()));
    
    // Order items
    List<Map<String, String>> orderItems = order.getLineItems().stream()
        .map(item -> {
            Map<String, String> itemMap = new HashMap<>();
            ProductVariant variant = item.getProductVariant();
            // Lấy variant với images từ map đã load trước
            ProductVariant variantWithImages = variantImageMap.get(variant.getId());
            
            itemMap.put("productName", variant.getProduct().getName());
            itemMap.put("color", variant.getColor().toString());
            itemMap.put("size", variant.getSize().toString());
            
            // Lấy URL hình ảnh từ variant đã được load với images
            String imageUrl = "";
            if (variantWithImages != null && !variantWithImages.getImages().isEmpty()) {
                imageUrl = variantWithImages.getImages().stream()
                    .filter(img -> img.getImageOrder() != null)
                    .min(Comparator.comparing(ProductImage::getImageOrder))
                    .map(ProductImage::getPublicUrl)
                    .orElseGet(() -> variantWithImages.getImages().get(0).getPublicUrl());
            }
            itemMap.put("imageUrl", imageUrl);
            
            itemMap.put("quantity", String.valueOf(item.getQuantity()));
            itemMap.put("unitPrice", String.format("%,.0f₫", item.getUnitPrice()));
            itemMap.put("discountAmount", String.format("%,.0f₫", item.getDiscountAmount()));
            itemMap.put("totalPrice", String.format("%,.0f₫", 
                (item.getUnitPrice() - item.getDiscountAmount()) * item.getQuantity()));
            return itemMap;
        })
        .collect(Collectors.toList());
    context.setVariable("orderItems", orderItems);

    // Format payment method based on enum
    String paymentMethodText = switch (order.getPaymentMethod()) {
        case COD -> "Thanh toán khi nhận hàng (COD)";
        case VNPAY -> "VNPay";
        default -> order.getPaymentMethod().toString();
    };
    context.setVariable("paymentMethod", paymentMethodText);

    // Format payment status based on enum
    String paymentStatusText = switch (order.getPaymentStatus()) {
        case PENDING -> "Chờ thanh toán";
        case SUCCESS -> "Đã thanh toán";
        case FAILED -> "Thanh toán thất bại";
        default -> order.getPaymentStatus().toString();
    };
    context.setVariable("paymentStatus", paymentStatusText);

    // Format delivery method based on enum
    String deliveryMethodText = switch (order.getDeliveryMethod()) {
        case GHN -> "Giao hàng nhanh";
        case EXPRESS -> "Giao hàng nhanh";
        default -> order.getDeliveryMethod().toString();
    };
    context.setVariable("shippingMethod", deliveryMethodText);

    // Recipient information
    ShippingInformation shipping = order.getShippingInformation();
    String recipientName = shipping.getFirstName() + " " + shipping.getLastName();
    context.setVariable("recipientName", recipientName);
    context.setVariable("recipientPhone", shipping.getPhoneNumber());

    // Format shipping address
    String shippingAddress = String.format("%s, %s, %s, %s, %s",
        shipping.getAddress(),
        shipping.getWard(),
        shipping.getDistrict(),
        shipping.getProvince(),
        shipping.getCountry());
    context.setVariable("shippingAddress", shippingAddress);

    // Estimated delivery
    context.setVariable("estimatedDelivery", "3-5 ngày làm việc");

    // Tracking URL
    context.setVariable("trackingUrl", baseUrl + "/orders/" + order.getCode());

    String content = templateEngine.process(AppConstant.ORDER_CONFIRMATION_EMAIL_TEMPLATE, context);
    this.sendEmailSync(order.getUser().getEmail(), AppConstant.ORDER_EMAIL_SUBJECT, content, true,
        true);
  }

  @Async
  @Override
  public void sendActivationCodeEmail(User user) {
    log.debug("Sending activation code email to '{}'", user.getEmail());
    this.sendEmailFromTemplateSync(user.getEmail(), AppConstant.ACTIVATION_CODE_EMAIL_SUBJECT,
        AppConstant.ACTIVATION_CODE_EMAIL_TEMPLATE, user.getProfile().getFirstName(),
        user.getActivationCode());
  }

  @Async
  @Override
  public void sendResetCodeEmail(User user) {
    log.debug("Sending reset code email to '{}'", user.getEmail());
    this.sendEmailFromTemplateSync(user.getEmail(), AppConstant.RESET_CODE_EMAIL_SUBJECT,
        AppConstant.RESET_CODE_EMAIL_TEMPLATE, user.getProfile().getFirstName(),
        user.getResetCode());
  }

  @Async
  @Override
  public void sendProfileOtpMobile(User user) {
    log.debug("Sending profile otp mobile to '{}'", user.getEmail());
    this.sendEmailFromTemplateSync(user.getEmail(), AppConstant.PROFILE_OTP_MOBILE_EMAIL_SUBJECT,
        AppConstant.PROFILE_OTP_MOBILE_EMAIL_TEMPLATE, user.getProfile().getFirstName(),
        user.getProfileCode());
  }
}

package com.example.clothingstore.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.exception.PaymentException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.service.EmailService;
import com.example.clothingstore.service.OderCancellationService;
import com.example.clothingstore.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.example.clothingstore.util.VnPayUtils;

@Service
@RequiredArgsConstructor
public class VnPayServiceImp implements VnPayService {

  @Value("${vnpay.tmncode}")
  private String vnp_TmnCode;

  @Value("${vnpay.hashsecret}")
  private String vnp_HashSecret;

  @Value("${vnpay.url}")
  private String vnp_PayUrl;

  @Value("${vnpay.returnurl}")
  private String vnp_ReturnUrl;

  private final Logger log = LoggerFactory.getLogger(VnPayServiceImp.class);

  private final OrderRepository orderRepository;

  private final EmailService emailService;

  private final OderCancellationService oderCancellationService;

  @Override
  public String createPaymentUrl(Order order, HttpServletRequest request) {
    String vnp_Version = "2.1.0";
    String vnp_Command = "pay";
    String orderType = "other";
    String vnp_IpAddr = VnPayUtils.getIpAddress(request);
    String vnp_TxnRef = order.getCode();
    String vnp_OrderInfo = "Thanh toan don hang: " + order.getCode();
    String vnp_OrderType = orderType;
    String vnp_Amount = String.valueOf(Math.round(order.getFinalTotal() * 100));
    String vnp_Locale = "vn";
    String vnp_CurrCode = "VND";
    String vnp_BankCode = "";

    ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
    Instant now = Instant.now();
    ZonedDateTime vietnamDateTime = now.atZone(vietnamZone);
    
    String vnp_CreateDate = DateTimeFormatter
        .ofPattern("yyyyMMddHHmmss")
        .format(vietnamDateTime);

    String vnp_ExpireDate = DateTimeFormatter
        .ofPattern("yyyyMMddHHmmss")
        .format(vietnamDateTime.plusMinutes(30));

    Map<String, String> vnp_Params = new HashMap<>();
    vnp_Params.put("vnp_Version", vnp_Version);
    vnp_Params.put("vnp_Command", vnp_Command);
    vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
    vnp_Params.put("vnp_Amount", vnp_Amount);
    vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
    vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
    vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
    vnp_Params.put("vnp_OrderType", vnp_OrderType);
    vnp_Params.put("vnp_Locale", vnp_Locale);
    vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
    vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
    vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
    vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

    if (vnp_BankCode != null && !vnp_BankCode.isEmpty()) {
      vnp_Params.put("vnp_BankCode", vnp_BankCode);
    }

    List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
    Collections.sort(fieldNames);
    StringBuilder hashData = new StringBuilder();
    StringBuilder query = new StringBuilder();
    Iterator<String> itr = fieldNames.iterator();
    while (itr.hasNext()) {
      String fieldName = itr.next();
      String fieldValue = vnp_Params.get(fieldName);
      if ((fieldValue != null) && (fieldValue.length() > 0)) {
        // Build hash data
        hashData.append(fieldName);
        hashData.append('=');
        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
        // Build query
        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
        query.append('=');
        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
        if (itr.hasNext()) {
          query.append('&');
          hashData.append('&');
        }
      }
    }
    String queryUrl = query.toString();
    String vnp_SecureHash = VnPayUtils.hmacSHA512(vnp_HashSecret, hashData.toString());
    queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
    return vnp_PayUrl + "?" + queryUrl;
  }

  @Override
  public Void validatePayment(HttpServletRequest request) {
    Map<String, String> fields = new HashMap<>();
    for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
      String fieldName = params.nextElement();
      String fieldValue = request.getParameter(fieldName);
      if ((fieldValue != null) && (fieldValue.length() > 0)) {
        fields.put(fieldName, fieldValue);
      }
    }

    String vnp_SecureHash = request.getParameter("vnp_SecureHash");
    String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
    String vnp_TxnRef = request.getParameter("vnp_TxnRef");
    String vnp_PayDate = request.getParameter("vnp_PayDate");

    // Remove unnecessary fields
    if (fields.containsKey("vnp_SecureHashType")) {
      fields.remove("vnp_SecureHashType");
    }
    if (fields.containsKey("vnp_SecureHash")) {
      fields.remove("vnp_SecureHash");
    }

    // Calculate checksum
    String signValue = hashAllFields(fields);

    if (signValue.equals(vnp_SecureHash)) {
      // Find order in database
      Order order = orderRepository.findByCode(vnp_TxnRef)
          .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

      if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
        return null;
      }

      if ("00".equals(vnp_ResponseCode)) {
        // Check amount
        long vnpAmount = Long.parseLong(fields.get("vnp_Amount")) / 100;
        if (vnpAmount != Math.round(order.getFinalTotal())) {
          throw new PaymentException(ErrorMessage.INVALID_AMOUNT);
        }

        // Convert vnp_PayDate to Instant
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        Instant paymentDate = Instant.from(formatter.parse(vnp_PayDate));

        // Update order status and payment date
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentDate(paymentDate);
        orderRepository.save(order);

        // Eagerly fetch order details
        Order orderWithDetails = orderRepository.findOrderWithDetailsById(order.getId());
        List<ProductVariant> productVariants = orderRepository
            .findProductVariantsWithImagesByOrderId(order.getId());
        emailService.sendOrderConfirmationEmail(orderWithDetails, productVariants);
      } else {
        log.info("Payment failed for order {}: Response code {}", vnp_TxnRef, vnp_ResponseCode);
        
        try {
          oderCancellationService.cancelOrderAndReturnStock(order.getId());
          order.setPaymentStatus(PaymentStatus.FAILED);
          orderRepository.save(order);
        } catch (Exception e) {
          log.error("Error while cancelling order and returning stock: ", e);
        }
        
        throw new PaymentException(ErrorMessage.PAYMENT_FAILED);
      }
      return null;
    } else {
      throw new PaymentException(ErrorMessage.INVALID_CHECKSUM);
    }
  }

  private String hashAllFields(Map<String, String> fields) {
    List<String> fieldNames = new ArrayList<>(fields.keySet());
    Collections.sort(fieldNames);
    StringBuilder hashData = new StringBuilder();
    Iterator<String> itr = fieldNames.iterator();
    while (itr.hasNext()) {
      String fieldName = itr.next();
      String fieldValue = fields.get(fieldName);
      if ((fieldValue != null) && (fieldValue.length() > 0)) {
        hashData.append(fieldName);
        hashData.append('=');
        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
        if (itr.hasNext()) {
          hashData.append('&');
        }
      }
    }
    return VnPayUtils.hmacSHA512(vnp_HashSecret, hashData.toString());
  }
}

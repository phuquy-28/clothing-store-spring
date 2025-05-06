package com.example.clothingstore.dto.response;

import java.time.Instant;
import java.util.List;

import com.example.clothingstore.enumeration.CashBackStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.ReturnRequestStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReturnRequestResDTO {

  private Long id;

  private Long orderId;

  private String orderCode;

  private ReturnRequestStatus status;

  private CashBackStatus cashBackStatus;

  private String reason;

  private Instant createdAt;

  private PaymentMethod originalPaymentMethod;

  private String bankName;

  private String accountNumber;

  private String accountHolderName;

  private String adminComment;

  private List<String> imageUrls;

  private OrderResDTO.LineItem[] orderItems;
}

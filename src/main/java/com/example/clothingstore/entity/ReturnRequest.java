package com.example.clothingstore.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.ReturnRequestStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest extends AbstractEntity {

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(columnDefinition = "text")
  private String reason;

  @Enumerated(EnumType.STRING)
  private ReturnRequestStatus status;

  @Enumerated(EnumType.STRING)
  private PaymentMethod originalPaymentMethod;

  private String bankName;

  private String accountNumber;

  private String accountHolderName;

  @Column(columnDefinition = "text")
  private String adminComment;

  @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReturnRequestImage> images = new ArrayList<>();
}

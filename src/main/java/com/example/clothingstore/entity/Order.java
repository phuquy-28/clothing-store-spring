package com.example.clothingstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.List;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AbstractEntity {

  private String code;

  private Instant orderDate;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Embedded
  private ShippingInformation shippingInformation;

  @Column(columnDefinition = "text")
  private String note;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LineItem> lineItems;

  private Double total;

  private Double shippingFee;

  private Double discount;

  private Double finalTotal;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  private Instant paymentDate;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ShippingInformation {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String address;

    private String district;

    private String province;

    private String country;
  }
}

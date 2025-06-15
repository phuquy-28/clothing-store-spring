package com.example.clothingstore.dto.response;

import com.example.clothingstore.enumeration.InventoryChangeType;
import lombok.Data;
import lombok.Builder;

import java.time.Instant;

@Data
@Builder
public class InventoryHistoryDTO {
  private Long id;

  private String productSku;

  private String productName;

  private Integer changeInQuantity;

  private Integer quantityAfterChange;

  private InventoryChangeType typeOfChange;

  private Instant timestamp;

  private String orderCode;

  private String notes;

  private String updatedBy;
}

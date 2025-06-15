package com.example.clothingstore.specification;

import com.example.clothingstore.entity.InventoryHistory;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class InventoryHistorySpecification {

  public static Specification<InventoryHistory> hasProductSku(String sku) {
    return (root, query, cb) -> {
      if (sku == null || sku.isEmpty()) {
        return cb.conjunction();
      }
      return cb.equal(root.get("productVariant").get("sku"), sku);
    };
  }

  public static Specification<InventoryHistory> hasTimestampAfter(Instant startDate) {
    return (root, query, cb) -> {
      if (startDate == null) {
        return cb.conjunction();
      }
      return cb.greaterThanOrEqualTo(root.get("timestamp"), startDate);
    };
  }

  public static Specification<InventoryHistory> hasTimestampBefore(Instant endDate) {
    return (root, query, cb) -> {
      if (endDate == null) {
        return cb.conjunction();
      }
      return cb.lessThanOrEqualTo(root.get("timestamp"), endDate);
    };
  }
}

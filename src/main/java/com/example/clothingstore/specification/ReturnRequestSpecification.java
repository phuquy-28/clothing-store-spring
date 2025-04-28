package com.example.clothingstore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.example.clothingstore.entity.ReturnRequest;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.ReturnRequestStatus;

public class ReturnRequestSpecification {

  private ReturnRequestSpecification() {
    // Private constructor to prevent instantiation
  }

  public static Specification<ReturnRequest> hasUser(User user) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
  }

  public static Specification<ReturnRequest> hasStatus(ReturnRequestStatus status) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
  }
}

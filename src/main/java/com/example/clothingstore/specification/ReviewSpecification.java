package com.example.clothingstore.specification;

import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.entity.Review;

public class ReviewSpecification {

  public static Specification<Review> hasProductSlug(String slug) {
    return (root, query, cb) -> {
      query.distinct(true);
      return cb.equal(root.get("product").get("slug"), slug);
    };
  }

  public static Specification<Review> hasRating(Integer rating) {
    return (root, query, cb) -> {
      if (rating == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("rating"), rating.doubleValue());
    };
  }

  public static Specification<Review> isPublished() {
    return (root, query, cb) -> cb.equal(root.get("published"), true);
  }

  public static Specification<Review> isNotDeleted() {
    return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
  }
}

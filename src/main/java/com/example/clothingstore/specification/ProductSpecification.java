package com.example.clothingstore.specification;

import java.time.Instant;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.entity.Category;
import com.example.clothingstore.entity.LineItem;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.Promotion;
import com.example.clothingstore.entity.Review;
import com.example.clothingstore.enumeration.PaymentStatus;

public class ProductSpecification {

  public static Specification<Product> isBestSeller(PaymentStatus paymentStatus,
      Instant startDate) {
    return (root, query, cb) -> {
      query.distinct(true);

      // Join từ LineItem đến Product thông qua productVariant.product
      Root<LineItem> lineItemRoot = query.from(LineItem.class);
      Join<LineItem, ProductVariant> variantJoin = lineItemRoot.join("productVariant");
      Join<ProductVariant, Product> productJoin = variantJoin.join("product");
      Join<LineItem, Order> orderJoin = lineItemRoot.join("order");

      // Điều kiện WHERE
      Predicate paymentStatusPred = cb.equal(orderJoin.get("paymentStatus"), paymentStatus);
      Predicate datePred = startDate == null ? cb.conjunction()
          : cb.greaterThanOrEqualTo(orderJoin.get("orderDate"), startDate);
      Predicate productPred = cb.equal(productJoin, root);

      // Group by và Order by
      if (query.getResultType() != Long.class) {
        query.groupBy(root);
        query.orderBy(cb.desc(cb.sum(lineItemRoot.get("quantity"))));
      }

      return cb.and(productPred, paymentStatusPred, datePred);
    };
  }

  public static Specification<Product> isDiscountedWithMaxDiscount(Instant now) {
    return (root, query, cb) -> {
      query.distinct(true);

      // Subquery cho Product Promotions - trả về Product thay vì Double
      Subquery<Product> productDiscountSubquery = query.subquery(Product.class);
      Root<Product> productRoot = productDiscountSubquery.from(Product.class);
      Join<Product, Promotion> productPromotionJoin = productRoot.join("promotions", JoinType.LEFT);

      productDiscountSubquery.select(productRoot)
          .where(cb.and(cb.lessThanOrEqualTo(productPromotionJoin.get("startDate"), now),
              cb.greaterThanOrEqualTo(productPromotionJoin.get("endDate"), now)));

      // Subquery cho Category - trả về Category thay vì Double
      Subquery<Category> categoryDiscountSubquery = query.subquery(Category.class);
      Root<Category> categoryRoot = categoryDiscountSubquery.from(Category.class);
      Join<Category, Promotion> categoryPromotionJoin =
          categoryRoot.join("promotions", JoinType.LEFT);

      categoryDiscountSubquery.select(categoryRoot)
          .where(cb.and(cb.lessThanOrEqualTo(categoryPromotionJoin.get("startDate"), now),
              cb.greaterThanOrEqualTo(categoryPromotionJoin.get("endDate"), now)));

      // Subquery cho việc sắp xếp theo discount rate
      Subquery<Double> maxDiscountSubquery = query.subquery(Double.class);
      Root<Product> discountRoot = maxDiscountSubquery.from(Product.class);
      Join<Product, Promotion> pPromotionJoin = discountRoot.join("promotions", JoinType.LEFT);
      Join<Product, Category> categoryJoin = discountRoot.join("category", JoinType.LEFT);
      Join<Category, Promotion> cPromotionJoin = categoryJoin.join("promotions", JoinType.LEFT);

      maxDiscountSubquery.select(cb
          .quot(cb.coalesce(cb.coalesce(cb.<Double>max(pPromotionJoin.<Double>get("discountRate")),
              cb.<Double>max(cPromotionJoin.<Double>get("discountRate"))), 0.0), 100.0)
          .as(Double.class)).where(cb.equal(discountRoot, root));

      // Sắp xếp theo discount rate
      if (query.getResultType() != Long.class) {
        query.orderBy(cb.desc(maxDiscountSubquery));
      }

      // Điều kiện lọc
      return cb.or(root.in(productDiscountSubquery),
          root.get("category").in(categoryDiscountSubquery));
    };
  }

  public static Specification<Product> sortBy(String sortField, String sortOrder) {
    return (root, query, cb) -> {
      if (query.getResultType() != Long.class) {
        switch (sortField) {
          case "createdAt":
            query.orderBy(sortOrder.equals("asc") ? cb.asc(root.get("createdAt"))
                : cb.desc(root.get("createdAt")));
            break;

          case "minPriceWithDiscount":
            // Subquery để tính giá thấp nhất của variant
            Subquery<Double> minVariantPrice = query.subquery(Double.class);
            Root<ProductVariant> variantRoot = minVariantPrice.from(ProductVariant.class);
            minVariantPrice.select(cb.min(variantRoot.get("differencePrice")))
                .where(cb.equal(variantRoot.get("product"), root))
                .groupBy(variantRoot.get("product"));

            // Subquery để tính discount rate cao nhất (chia cho 100)
            Subquery<Double> maxDiscount = query.subquery(Double.class);
            Root<Product> discountRoot = maxDiscount.from(Product.class);
            Join<Product, Promotion> productPromotionJoin =
                discountRoot.join("promotions", JoinType.LEFT);
            Join<Product, Category> categoryJoin = discountRoot.join("category", JoinType.LEFT);
            Join<Category, Promotion> categoryPromotionJoin =
                categoryJoin.join("promotions", JoinType.LEFT);

            maxDiscount.select(cb.quot(
                cb.coalesce(
                    cb.coalesce(cb.<Double>max(productPromotionJoin.<Double>get("discountRate")),
                        cb.<Double>max(categoryPromotionJoin.<Double>get("discountRate"))),
                    0.0),
                100.0).as(Double.class)).where(cb.equal(discountRoot, root)).groupBy(discountRoot);

            // Tính giá cuối cùng
            Expression<Double> finalPrice = cb.diff(cb.sum(root.get("price"), minVariantPrice),
                cb.prod(cb.sum(root.get("price"), minVariantPrice), maxDiscount));

            query.orderBy(sortOrder.equals("asc") ? cb.asc(finalPrice) : cb.desc(finalPrice));
            break;
        }
      }
      return cb.conjunction();
    };
  }

  public static Specification<Product> averageRatingGreaterThanOrEqualTo(Double averageRating) {
    return (root, query, cb) -> {
      query.distinct(true);
      Subquery<Double> avgRatingSubquery = query.subquery(Double.class);
      Root<Review> reviewRoot = avgRatingSubquery.from(Review.class);
      avgRatingSubquery.select(cb.avg(reviewRoot.get("rating")))
          .where(cb.equal(reviewRoot.get("product"), root));
      return cb.greaterThanOrEqualTo(avgRatingSubquery, averageRating);
    };
  }

  public static Specification<Product> minPriceGreaterThanOrEqualTo(Double minPrice) {
    return (root, query, cb) -> {
      Instant now = Instant.now();

      // Subquery để tính giá thấp nhất của variant
      Subquery<Double> minVariantPrice = query.subquery(Double.class);
      Root<ProductVariant> variantRoot = minVariantPrice.from(ProductVariant.class);
      minVariantPrice.select(cb.min(variantRoot.get("differencePrice")))
          .where(cb.equal(variantRoot.get("product"), root)).groupBy(variantRoot.get("product"));

      // Subquery để tính discount rate cao nhất
      Subquery<Double> maxDiscount = query.subquery(Double.class);
      Root<Product> discountRoot = maxDiscount.from(Product.class);
      Join<Product, Promotion> productPromotionJoin =
          discountRoot.join("promotions", JoinType.LEFT);
      Join<Product, Category> categoryJoin = discountRoot.join("category", JoinType.LEFT);
      Join<Category, Promotion> categoryPromotionJoin =
          categoryJoin.join("promotions", JoinType.LEFT);

      // Tính max discount rate cho product promotions
      Expression<Double> productDiscountRate = cb.<Double>selectCase()
          .when(
              cb.and(cb.lessThanOrEqualTo(productPromotionJoin.get("startDate"), now),
                  cb.greaterThanOrEqualTo(productPromotionJoin.get("endDate"), now)),
              productPromotionJoin.get("discountRate"))
          .otherwise(0.0);

      // Tính max discount rate cho category promotions
      Expression<Double> categoryDiscountRate = cb.<Double>selectCase()
          .when(
              cb.and(cb.lessThanOrEqualTo(categoryPromotionJoin.get("startDate"), now),
                  cb.greaterThanOrEqualTo(categoryPromotionJoin.get("endDate"), now)),
              categoryPromotionJoin.get("discountRate"))
          .otherwise(0.0);

      maxDiscount
          .select(cb.quot(cb.coalesce(
              cb.max(
                  cb.function("GREATEST", Double.class, productDiscountRate, categoryDiscountRate)),
              0.0).as(Double.class), cb.literal(100.0)).as(Double.class))
          .where(cb.equal(discountRoot, root)).groupBy(discountRoot);

      // Tính giá cuối cùng và so sánh với minPrice
      return cb.greaterThanOrEqualTo(cb.diff(cb.sum(root.get("price"), minVariantPrice),
          cb.prod(cb.sum(root.get("price"), minVariantPrice), maxDiscount)), minPrice);
    };
  }

  public static Specification<Product> maxPriceLessThanOrEqualTo(Double maxPrice) {
    return (root, query, cb) -> {
      Instant now = Instant.now();

      // Subquery để tính giá cao nhất của variant
      Subquery<Double> maxVariantPrice = query.subquery(Double.class);
      Root<ProductVariant> variantRoot = maxVariantPrice.from(ProductVariant.class);
      maxVariantPrice.select(cb.max(variantRoot.get("differencePrice")))
          .where(cb.equal(variantRoot.get("product"), root)).groupBy(variantRoot.get("product"));

      // Subquery để tính discount rate cao nhất
      Subquery<Double> maxDiscount = query.subquery(Double.class);
      Root<Product> discountRoot = maxDiscount.from(Product.class);
      Join<Product, Promotion> productPromotionJoin =
          discountRoot.join("promotions", JoinType.LEFT);
      Join<Product, Category> categoryJoin = discountRoot.join("category", JoinType.LEFT);
      Join<Category, Promotion> categoryPromotionJoin =
          categoryJoin.join("promotions", JoinType.LEFT);

      // Tính max discount rate cho product promotions
      Expression<Double> productDiscountRate = cb.<Double>selectCase()
          .when(
              cb.and(cb.lessThanOrEqualTo(productPromotionJoin.get("startDate"), now),
                  cb.greaterThanOrEqualTo(productPromotionJoin.get("endDate"), now)),
              productPromotionJoin.get("discountRate"))
          .otherwise(0.0);

      // Tính max discount rate cho category promotions
      Expression<Double> categoryDiscountRate = cb.<Double>selectCase()
          .when(
              cb.and(cb.lessThanOrEqualTo(categoryPromotionJoin.get("startDate"), now),
                  cb.greaterThanOrEqualTo(categoryPromotionJoin.get("endDate"), now)),
              categoryPromotionJoin.get("discountRate"))
          .otherwise(0.0);

      maxDiscount
          .select(cb.quot(cb.coalesce(
              cb.max(
                  cb.function("GREATEST", Double.class, productDiscountRate, categoryDiscountRate)),
              0.0).as(Double.class), cb.literal(100.0)).as(Double.class))
          .where(cb.equal(discountRoot, root)).groupBy(discountRoot);

      // Tính giá cuối cùng và so sánh với maxPrice
      return cb.lessThanOrEqualTo(cb.diff(cb.sum(root.get("price"), maxVariantPrice),
          cb.prod(cb.sum(root.get("price"), maxVariantPrice), maxDiscount)), maxPrice);
    };
  }

}

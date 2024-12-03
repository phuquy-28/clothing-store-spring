package com.example.clothingstore.service.impl;

import java.time.Instant;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.Promotion;
import com.example.clothingstore.service.PromotionCalculatorService;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionCalculatorServiceImpl implements PromotionCalculatorService {
  
  @Override
  public Double calculateDiscountRate(Product product) {
    if (product == null) {
      return 0.0;
    }

    Instant now = Instant.now();

    Double maxProductDiscount = 0.0;
    if (product.getPromotions() != null) {
      maxProductDiscount = product.getPromotions().stream()
          .filter(promotion -> promotion != null 
              && promotion.getStartDate() != null 
              && promotion.getEndDate() != null
              && promotion.getStartDate().isBefore(now)
              && promotion.getEndDate().isAfter(now))
          .map(Promotion::getDiscountRate)
          .max(Double::compare)
          .orElse(0.0);
    }

    Double maxCategoryDiscount = 0.0;
    if (product.getCategory() != null && product.getCategory().getPromotions() != null) {
      maxCategoryDiscount = product.getCategory().getPromotions().stream()
          .filter(promotion -> promotion != null
              && promotion.getStartDate() != null
              && promotion.getEndDate() != null 
              && promotion.getStartDate().isBefore(now)
              && promotion.getEndDate().isAfter(now))
          .map(Promotion::getDiscountRate)
          .max(Double::compare)
          .orElse(0.0);
    }

    return Math.max(maxProductDiscount, maxCategoryDiscount) / 100;
  }

  @Override
  public Double calculateMinPrice(Product product) {
    if (product == null) {
      return 0.0;
    }

    return Math.round(product.getVariants().stream()
        .mapToDouble(variant -> variant.getDifferencePrice() + product.getPrice())
        .min()
        .orElse(0.0) * 10.0) / 10.0;
  }

  @Override
  public Double calculateMaxPrice(Product product) {
    if (product == null) {
      return 0.0;
    }

    return Math.round(product.getVariants().stream()
        .mapToDouble(variant -> variant.getDifferencePrice() + product.getPrice())
        .max()
        .orElse(0.0) * 10.0) / 10.0;
  }

  @Override
  public Double calculatePriceWithDiscount(Product product) {
    if (product == null) {
      return 0.0;
    }

    return Math.round(product.getPrice() * (1 - calculateDiscountRate(product)) * 10.0) / 10.0;
  }

  @Override
  public Double calculateMinPriceWithDiscount(Product product) {
    if (product == null) {
      return 0.0;
    }

    return Math.round(calculateMinPrice(product) * (1 - calculateDiscountRate(product)) * 10.0) / 10.0;
  }

  @Override
  public Double calculateMaxPriceWithDiscount(Product product) {
    if (product == null) {
      return 0.0;
    }

    return Math.round(calculateMaxPrice(product) * (1 - calculateDiscountRate(product)) * 10.0) / 10.0;
  }
}

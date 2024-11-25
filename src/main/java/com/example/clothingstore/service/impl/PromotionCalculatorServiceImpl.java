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
    Instant now = Instant.now();

    Double maxProductDiscount = product.getPromotions().stream()
        .filter(promotion -> promotion.getStartDate().isBefore(now)
            && promotion.getEndDate().isAfter(now))
        .map(Promotion::getDiscountRate)
        .max(Double::compare)
        .orElse(0.0);

    Double maxCategoryDiscount = product.getCategory().getPromotions().stream()
        .filter(promotion -> promotion.getStartDate().isBefore(now)
            && promotion.getEndDate().isAfter(now))
        .map(Promotion::getDiscountRate)
        .max(Double::compare)
        .orElse(0.0);

    return Math.max(maxProductDiscount, maxCategoryDiscount);
  }
}

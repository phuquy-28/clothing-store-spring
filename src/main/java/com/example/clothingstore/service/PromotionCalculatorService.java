package com.example.clothingstore.service;

import com.example.clothingstore.entity.Product;

public interface PromotionCalculatorService {
  Double calculateDiscountRate(Product product);

  Double calculateMinPrice(Product product);

  Double calculateMaxPrice(Product product);

  Double calculatePriceWithDiscount(Product product);

  Double calculateMinPriceWithDiscount(Product product);

  Double calculateMaxPriceWithDiscount(Product product);
}

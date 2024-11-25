package com.example.clothingstore.service;

import com.example.clothingstore.entity.Product;

public interface PromotionCalculatorService {
  Double calculateDiscountRate(Product product);
}

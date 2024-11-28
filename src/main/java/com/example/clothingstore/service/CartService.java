package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.AddToCartDTO;
import com.example.clothingstore.dto.response.CartItemDTO;
import java.util.List;

public interface CartService {

  void addToCart(AddToCartDTO addToCartDTO);

  List<CartItemDTO> getCartItems();

  void updateCartItem(CartItemDTO cartItemDTO);

  void deleteCartItem(Long cartItemId);

  Long getCartItemsCount();
}

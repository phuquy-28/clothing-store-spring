package com.example.clothingstore.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.AddToCartDTO;
import com.example.clothingstore.dto.response.CartItemDTO;
import com.example.clothingstore.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @PostMapping(UrlConfig.CART + UrlConfig.ITEMS)
  public ResponseEntity<Void> addToCart(@RequestBody @Valid AddToCartDTO addToCartDTO) {
    cartService.addToCart(addToCartDTO);
    return ResponseEntity.ok().build();
  }

  @GetMapping(UrlConfig.CART + UrlConfig.ITEMS)
  public ResponseEntity<List<CartItemDTO>> getCartItems() {
    return ResponseEntity.ok(cartService.getCartItems());
  }

  @PutMapping(UrlConfig.CART + UrlConfig.ITEMS)
  public ResponseEntity<Void> updateCartItem(@RequestBody @Valid CartItemDTO cartItemDTO) {
    cartService.updateCartItem(cartItemDTO);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping(UrlConfig.CART + UrlConfig.ITEMS + "/{cartItemId}")
  public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
    cartService.deleteCartItem(cartItemId);
    return ResponseEntity.ok().build();
  }
}

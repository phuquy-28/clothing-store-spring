package com.example.clothingstore.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.AddToCartDTO;
import com.example.clothingstore.dto.response.CartItemDTO;
import com.example.clothingstore.entity.Cart;
import com.example.clothingstore.entity.CartItem;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.CartRepository;
import com.example.clothingstore.repository.ProductVariantRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.CartService;
import com.example.clothingstore.service.PromotionCalculatorService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;

  private final UserRepository userRepository;

  private final ProductVariantRepository productVariantRepository;

  private final PromotionCalculatorService promotionCalculatorService;

  @Override
  public void addToCart(AddToCartDTO addToCartDTO) {
    String email = SecurityUtil.getCurrentUserLogin().get();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    ProductVariant productVariant =
        productVariantRepository.findById(addToCartDTO.getProductVariantId()).orElseThrow(
            () -> new ResourceNotFoundException(ErrorMessage.PRODUCT_VARIANT_NOT_FOUND));

    // Kiểm tra số lượng
    if (productVariant.getQuantity() < addToCartDTO.getQuantity()) {
      throw new BadRequestException(ErrorMessage.NOT_ENOUGH_STOCK);
    }

    // Lấy hoặc tạo giỏ hàng cho user
    Cart cart = user.getCart();
    if (cart == null) {
      cart = new Cart();
      cart.setUser(user);
      cart.setCartItems(new ArrayList<>());
      user.setCart(cart);
    }

    // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
    Optional<CartItem> existingItem = cart.getCartItems().stream()
        .filter(item -> item.getProductVariant().getId().equals(addToCartDTO.getProductVariantId()))
        .findFirst();

    if (existingItem.isPresent()) {
      // Cập nhật số lượng nếu sản phẩm đã tồn tại
      CartItem cartItem = existingItem.get();
      int newQuantity = cartItem.getQuantity() + addToCartDTO.getQuantity();
      if (newQuantity > productVariant.getQuantity()) {
        throw new BadRequestException(ErrorMessage.NOT_ENOUGH_STOCK);
      }
      cartItem.setQuantity(newQuantity);
    } else {
      // Thêm sản phẩm mới vào giỏ hàng
      CartItem cartItem = new CartItem();
      cartItem.setCart(cart);
      cartItem.setProductVariant(productVariant);
      cartItem.setQuantity(addToCartDTO.getQuantity());
      cart.getCartItems().add(cartItem);
    }

    cartRepository.save(cart);
  }

  @Override
  public List<CartItemDTO> getCartItems() {
    String email = SecurityUtil.getCurrentUserLogin()
        .orElseThrow(() -> new BadRequestException(ErrorMessage.USER_NOT_LOGGED_IN));

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    Cart cart = user.getCart();
    if (cart == null || cart.getCartItems().isEmpty()) {
      return new ArrayList<>();
    }

    return cart.getCartItems().stream().map(cartItem -> {
      ProductVariant variant = cartItem.getProductVariant();
      Product product = variant.getProduct();

      // Tính giá gốc = giá sản phẩm + chênh lệch giá của variant
      Double originalPrice = product.getPrice()
          + (variant.getDifferencePrice() != null ? variant.getDifferencePrice() : 0.0);

      // Tính giá khuyến mãi
      Double discountRate = promotionCalculatorService.calculateDiscountRate(product);
      Double finalPrice = originalPrice * (1 - discountRate);

      return CartItemDTO.builder().cartItemId(cartItem.getId()).productName(product.getName())
          .productVariant(CartItemDTO.ProductVariantDTO.builder().id(variant.getId())
              .color(variant.getColor().toString()).size(variant.getSize().toString())
              .image(variant.getImages().get(0).getPublicUrl()).build())
          .price(originalPrice).finalPrice(finalPrice).quantity(cartItem.getQuantity())
          .inStock(variant.getQuantity()).image(product.getImages().get(0).getPublicUrl()).build();
    }).toList();
  }

  @Override
  public void updateCartItem(CartItemDTO cartItemDTO) {
    // Lấy thông tin user hiện tại
    String email = SecurityUtil.getCurrentUserLogin()
        .orElseThrow(() -> new BadRequestException(ErrorMessage.USER_NOT_LOGGED_IN));

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    Cart cart = user.getCart();
    if (cart == null) {
      throw new ResourceNotFoundException(ErrorMessage.CART_NOT_FOUND);
    }

    // Tìm cart item cần update
    CartItem cartItem = cart.getCartItems().stream()
        .filter(item -> item.getId().equals(cartItemDTO.getCartItemId())).findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CART_ITEM_NOT_FOUND));

    // Kiểm tra số lượng tồn kho
    ProductVariant productVariant = cartItem.getProductVariant();
    if (productVariant.getQuantity() < cartItemDTO.getQuantity()) {
      throw new BadRequestException(ErrorMessage.NOT_ENOUGH_STOCK);
    }

    // Nếu số lượng = 0, xóa item khỏi giỏ hàng
    if (cartItemDTO.getQuantity() <= 0) {
      cart.getCartItems().remove(cartItem);
    } else {
      // Cập nhật số lượng mới
      cartItem.setQuantity(cartItemDTO.getQuantity());
    }

    cartRepository.save(cart);
  }

  @Override
  public void deleteCartItem(Long cartItemId) {
    // Lấy thông tin user hiện tại
    String email = SecurityUtil.getCurrentUserLogin()
        .orElseThrow(() -> new BadRequestException(ErrorMessage.USER_NOT_LOGGED_IN));

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    Cart cart = user.getCart();
    if (cart == null) {
      throw new ResourceNotFoundException(ErrorMessage.CART_NOT_FOUND);
    }

    // Tìm và xóa cart item
    CartItem cartItemToRemove =
        cart.getCartItems().stream().filter(item -> item.getId().equals(cartItemId)).findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CART_ITEM_NOT_FOUND));

    cart.getCartItems().remove(cartItemToRemove);
    cartRepository.save(cart);
  }
}

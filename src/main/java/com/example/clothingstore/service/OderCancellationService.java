package com.example.clothingstore.service;

public interface OderCancellationService {

  void cancelOrderAndReturnStock(Long orderId);

  void userCancelOrder(Long orderId, String reason);
}

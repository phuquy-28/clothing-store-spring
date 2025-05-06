package com.example.clothingstore.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.ReturnRequestProcessDTO;
import com.example.clothingstore.dto.request.ReturnRequestReqDTO;
import com.example.clothingstore.dto.request.CashBackUpdateDTO;
import com.example.clothingstore.dto.response.OrderResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ReturnRequestResDTO;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.ReturnRequest;
import com.example.clothingstore.entity.ReturnRequestImage;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.ReturnRequestStatus;
import com.example.clothingstore.enumeration.CashBackStatus;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.repository.ReturnRequestRepository;
import com.example.clothingstore.service.ReturnRequestService;
import com.example.clothingstore.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

  private final Logger log = LoggerFactory.getLogger(ReturnRequestServiceImpl.class);

  private final ReturnRequestRepository returnRequestRepository;

  private final OrderRepository orderRepository;

  private final SecurityUtil securityUtil;

  @Override
  @Transactional
  public ReturnRequestResDTO createReturnRequest(ReturnRequestReqDTO returnRequestReqDTO) {
    // Get the current user
    User currentUser = securityUtil.getCurrentUser();

    // Find the order
    Order order = orderRepository.findById(returnRequestReqDTO.getOrderId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    // Verify the order belongs to the current user
    if (!order.getUser().getId().equals(currentUser.getId())) {
      throw new BadRequestException(ErrorMessage.USER_NOT_AUTHORIZED);
    }

    // Check if order is eligible for return (must be DELIVERED and within 30 days)
    if (order.getStatus() != OrderStatus.DELIVERED || order.getStatusHistories().stream()
        .filter(statusHistory -> statusHistory.getNewStatus().equals(order.getStatus()))
        .anyMatch(statusHistory -> statusHistory.getUpdateTimestamp()
            .isBefore(Instant.now().minus(30, ChronoUnit.DAYS)))) {
      throw new BadRequestException(ErrorMessage.ORDER_CAN_NOT_BE_RETURNED);
    }

    // Check if a return request already exists for this order
    if (returnRequestRepository.existsByOrder(order)) {
      throw new BadRequestException(ErrorMessage.RETURN_REQUEST_EXISTS);
    }

    // Validate bank information for COD orders
    if (order.getPaymentMethod().name().equals("COD")) {
      if (returnRequestReqDTO.getBankName() == null || returnRequestReqDTO.getBankName().isBlank()
          || returnRequestReqDTO.getAccountNumber() == null
          || returnRequestReqDTO.getAccountNumber().isBlank()
          || returnRequestReqDTO.getAccountHolderName() == null
          || returnRequestReqDTO.getAccountHolderName().isBlank()) {
        throw new BadRequestException(ErrorMessage.BANK_INFORMATION_REQUIRED);
      }
    }

    // Create return request
    ReturnRequest returnRequest = new ReturnRequest();
    returnRequest.setOrder(order);
    returnRequest.setUser(currentUser);
    returnRequest.setReason(returnRequestReqDTO.getReason());
    returnRequest.setStatus(ReturnRequestStatus.PENDING);
    returnRequest.setCashBackStatus(null);
    returnRequest.setOriginalPaymentMethod(order.getPaymentMethod());

    // Set bank information if provided
    if (order.getPaymentMethod().name().equals("COD")) {
      returnRequest.setBankName(returnRequestReqDTO.getBankName());
      returnRequest.setAccountNumber(returnRequestReqDTO.getAccountNumber());
      returnRequest.setAccountHolderName(returnRequestReqDTO.getAccountHolderName());
    }

    // Add images if provided
    if (returnRequestReqDTO.getImageUrls() != null
        && returnRequestReqDTO.getImageUrls().length > 0) {
      List<ReturnRequestImage> images = new ArrayList<>();
      for (String imageUrl : returnRequestReqDTO.getImageUrls()) {
        ReturnRequestImage image = new ReturnRequestImage();
        image.setImageUrl(imageUrl);
        image.setReturnRequest(returnRequest);
        images.add(image);
      }
      returnRequest.setImages(images);
    }

    // Save return request
    ReturnRequest saved = returnRequestRepository.save(returnRequest);

    // Update order status
    order.setStatus(OrderStatus.RETURNED);
    orderRepository.save(order);

    // Convert to DTO and return
    return convertToDTO(saved);
  }

  @Override
  @Transactional
  public ReturnRequestResDTO processReturnRequest(ReturnRequestProcessDTO returnRequestProcessDTO) {
    // Find the return request
    ReturnRequest returnRequest =
        returnRequestRepository.findById(returnRequestProcessDTO.getReturnRequestId())
            .orElseThrow(() -> new ResourceNotFoundException("error.return.request.not.found"));

    // Parse the status
    ReturnRequestStatus newStatus =
        ReturnRequestStatus.valueOf(returnRequestProcessDTO.getStatus().toUpperCase());

    // Update the return request
    returnRequest.setStatus(newStatus);
    returnRequest.setAdminComment(returnRequestProcessDTO.getAdminComment());

    // If approved, process refund
    if (newStatus == ReturnRequestStatus.APPROVED) {
      // Update cashback status to ACCEPTED when return request is approved
      returnRequest.setCashBackStatus(CashBackStatus.ACCEPTED);

      // For VNPAY payments, we would initiate a refund through VNPAY
      if (returnRequest.getOriginalPaymentMethod().name().equals("VNPAY")) {
        // This is a placeholder - in a real implementation, you would call the VNPay refund API
        log.info("Processing VNPAY refund for order: {}", returnRequest.getOrder().getCode());
        // vnPayService.processRefund(returnRequest.getOrder().getCode(),
        // returnRequest.getOrder().getFinalTotal());
      }

      // Update order status to RETURNED
      Order order = returnRequest.getOrder();
      order.setStatus(OrderStatus.RETURNED);
      orderRepository.save(order);
    }

    // Save the updated return request
    ReturnRequest updated = returnRequestRepository.save(returnRequest);

    // Convert to DTO and return
    return convertToDTO(updated);
  }

  @Override
  public ReturnRequestResDTO getReturnRequestById(Long id) {
    ReturnRequest returnRequest = returnRequestRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.RETURN_REQUEST_NOT_FOUND));

    // Check if current user has access to this return request
    User currentUser = securityUtil.getCurrentUser();
    if (!currentUser.getRole().getName().equals(AppConstant.ROLE_ADMIN)
        && !returnRequest.getUser().getId().equals(currentUser.getId())) {
      throw new BadRequestException(ErrorMessage.USER_NOT_AUTHORIZED);
    }

    return convertToDTO(returnRequest);
  }

  @Override
  public ResultPaginationDTO getAllReturnRequests(Specification<ReturnRequest> spec,
      Pageable pageable) {
    // Only admins should access all return requests
    User currentUser = securityUtil.getCurrentUser();
    if (!currentUser.getRole().getName().equals(AppConstant.ROLE_ADMIN)) {
      throw new BadRequestException(ErrorMessage.USER_NOT_AUTHORIZED);
    }

    Page<ReturnRequest> returnRequests = returnRequestRepository.findAll(spec, pageable);

    List<ReturnRequestResDTO> content =
        returnRequests.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());

    ResultPaginationDTO.Meta meta =
        ResultPaginationDTO.Meta.builder().page((long) returnRequests.getNumber())
            .pageSize((long) returnRequests.getSize()).pages((long) returnRequests.getTotalPages())
            .total(returnRequests.getTotalElements()).build();

    return ResultPaginationDTO.builder().meta(meta).data(content).build();
  }

  @Override
  @Transactional
  public void deleteReturnRequest(Long id) {
    ReturnRequest returnRequest = returnRequestRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.RETURN_REQUEST_NOT_FOUND));

    // Only the user who created the request can delete it, and only if it's in PENDING status
    User currentUser = securityUtil.getCurrentUser();
    if (!returnRequest.getUser().getId().equals(currentUser.getId())) {
      throw new BadRequestException(ErrorMessage.USER_NOT_AUTHORIZED);
    }

    if (returnRequest.getStatus() != ReturnRequestStatus.PENDING) {
      throw new BadRequestException("error.return.request.cannot.delete");
    }

    returnRequest.setStatus(ReturnRequestStatus.CANCELED);

    returnRequestRepository.save(returnRequest);
  }

  @Override
  @Transactional
  public ReturnRequestResDTO updateCashBackStatus(CashBackUpdateDTO cashBackUpdateDTO) {
    ReturnRequest returnRequest =
        returnRequestRepository.findById(cashBackUpdateDTO.getReturnRequestId()).orElseThrow(
            () -> new ResourceNotFoundException(ErrorMessage.RETURN_REQUEST_NOT_FOUND));

    if (returnRequest.getStatus() != ReturnRequestStatus.APPROVED) {
      throw new BadRequestException(ErrorMessage.RETURN_REQUEST_NOT_APPROVED);
    }

    CashBackStatus newCashBackStatus;
    try {
      newCashBackStatus =
          CashBackStatus.valueOf(cashBackUpdateDTO.getCashBackStatus().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(ErrorMessage.CASHBACK_STATUS_INVALID);
    }

    returnRequest.setCashBackStatus(newCashBackStatus);

    ReturnRequest updated = returnRequestRepository.save(returnRequest);

    return convertToDTO(updated);
  }

  private ReturnRequestResDTO convertToDTO(ReturnRequest returnRequest) {
    List<String> imageUrls = returnRequest.getImages().stream().map(ReturnRequestImage::getImageUrl)
        .collect(Collectors.toList());

    OrderResDTO.LineItem[] orderItems =
        returnRequest.getOrder().getLineItems().stream().map(lineItem -> {
          String variantImage = "";
          if (lineItem.getProductVariant() != null
              && lineItem.getProductVariant().getImages() != null
              && !lineItem.getProductVariant().getImages().isEmpty()) {
            variantImage = lineItem.getProductVariant().getImages().get(0).getPublicUrl();
          }

          return OrderResDTO.LineItem.builder().id(lineItem.getId())
              .productName(lineItem.getProductVariant().getProduct().getName())
              .color(lineItem.getProductVariant().getColor())
              .size(lineItem.getProductVariant().getSize()).variantImage(variantImage)
              .quantity(lineItem.getQuantity()).unitPrice(lineItem.getUnitPrice())
              .discount(lineItem.getDiscountAmount()).build();
        }).toArray(OrderResDTO.LineItem[]::new);

    return ReturnRequestResDTO.builder().id(returnRequest.getId())
        .orderId(returnRequest.getOrder().getId()).orderCode(returnRequest.getOrder().getCode())
        .status(returnRequest.getStatus()).cashBackStatus(returnRequest.getCashBackStatus())
        .reason(returnRequest.getReason()).createdAt(returnRequest.getCreatedAt())
        .originalPaymentMethod(returnRequest.getOriginalPaymentMethod())
        .bankName(returnRequest.getBankName()).accountNumber(returnRequest.getAccountNumber())
        .accountHolderName(returnRequest.getAccountHolderName())
        .adminComment(returnRequest.getAdminComment()).imageUrls(imageUrls).orderItems(orderItems)
        .build();
  }

  @Override
  public ReturnRequestResDTO getReturnRequestByOrderId(Long id) {
    ReturnRequest returnRequest = returnRequestRepository.findByOrderId(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.RETURN_REQUEST_NOT_FOUND));

    // Check if current user has access to this return request
    User currentUser = securityUtil.getCurrentUser();
    if (!currentUser.getRole().getName().equals(AppConstant.ROLE_ADMIN)
        && !returnRequest.getUser().getId().equals(currentUser.getId())) {
      throw new BadRequestException(ErrorMessage.USER_NOT_AUTHORIZED);
    }

    return convertToDTO(returnRequest);
  }
}

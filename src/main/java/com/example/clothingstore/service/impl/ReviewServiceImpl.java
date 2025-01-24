package com.example.clothingstore.service.impl;

import java.time.ZoneOffset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.PublishReviewReqDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ReviewDTO;
import com.example.clothingstore.dto.response.ReviewDTO.UserReviewDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO.Meta;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Review;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.ReviewRepository;
import com.example.clothingstore.service.ReviewService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;

  @Override
  public ResultPaginationDTO getReviews(Specification<Review> specification, Pageable pageable) {
    Page<Review> reviews = reviewRepository.findAll(specification, pageable);

    return ResultPaginationDTO.builder()
        .meta(Meta.builder().page((long) reviews.getNumber()).pageSize((long) reviews.getSize())
            .pages((long) reviews.getTotalPages()).total(reviews.getTotalElements()).build())
        .data(reviews.getContent().stream().map(this::convertToDTO).toList()).build();

  }

  @Override
  public ReviewDTO publishReview(PublishReviewReqDTO reqDTO) {
    Review review = reviewRepository.findById(reqDTO.getReviewId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.REVIEW_NOT_FOUND));
    review.setPublished(reqDTO.getPublished());
    reviewRepository.save(review);
    return convertToDTO(review);
  }

  @Override
  public void deleteReview(Long id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.REVIEW_NOT_FOUND));
    reviewRepository.delete(review);
  }

  private ReviewDTO convertToDTO(Review review) {
    return ReviewDTO.builder().reviewId(review.getId()).description(review.getDescription())
        .rating(review.getRating())
        .createdAt(review.getCreatedAt() != null
            ? review.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDateTime()
            : null)
        .isPublished(review.isPublished())
        .userReviewDTO(UserReviewDTO.builder().id(review.getUser().getId())
            .firstName(review.getUser().getProfile().getFirstName())
            .lastName(review.getUser().getProfile().getLastName())
            .email(review.getUser().getEmail()).totalSpend(calculateTotalSpend(review.getUser()))
            .totalReview(calculateTotalReview(review.getUser())).build())
        .build();
  }

  private Double calculateTotalSpend(User user) {
    return user.getOrders().stream()
        .filter(order -> order.getStatus() == OrderStatus.DELIVERED
            && order.getPaymentStatus() == PaymentStatus.SUCCESS)
        .mapToDouble(Order::getFinalTotal).sum();
  }

  private Long calculateTotalReview(User user) {
    return user.getReviews().stream().filter(review -> review.isPublished()).count();
  }
}

package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.PublishReviewReqDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ReviewDTO;
import com.example.clothingstore.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ReviewService {

  ResultPaginationDTO getReviews(Specification<Review> specification, Pageable pageable);

  ReviewDTO publishReview(PublishReviewReqDTO reqDTO);

  void deleteReview(Long id);
}


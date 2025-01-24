package com.example.clothingstore.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.PublishReviewReqDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ReviewDTO;
import com.example.clothingstore.entity.Review;
import com.example.clothingstore.service.ReviewService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @GetMapping(UrlConfig.REVIEW)
  public ResponseEntity<ResultPaginationDTO> getReviews(@Filter Specification<Review> specification,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC,
          size = 20) Pageable pageable) {
    return ResponseEntity.ok(reviewService.getReviews(specification, pageable));
  }

  @PostMapping(UrlConfig.REVIEW + UrlConfig.PUBLISH)
  public ResponseEntity<ReviewDTO> publishReview(@RequestBody @Valid PublishReviewReqDTO reqDTO) {
    return ResponseEntity.ok(reviewService.publishReview(reqDTO));
  }

  @DeleteMapping(UrlConfig.REVIEW + UrlConfig.ID)
  public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
    reviewService.deleteReview(id);
    return ResponseEntity.ok().build();
  }
}


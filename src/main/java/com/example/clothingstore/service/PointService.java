package com.example.clothingstore.service;

import com.example.clothingstore.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.dto.request.PointHistoryReqDTO;
import com.example.clothingstore.dto.response.PointHistoryDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Point;
import com.example.clothingstore.entity.PointHistory;
import com.example.clothingstore.dto.response.PointUserCurrentResDTO;

public interface PointService {

  void addPointsFromOrder(Order order);

  void refundPointsFromOrder(Order order);

  Long calculatePointsFromAmount(Double amount);

  Double calculateAmountFromPoints(Long points);

  ResultPaginationDTO getUserPointHistory(Specification<PointHistory> spec, Pageable pageable);

  ResultPaginationDTO getPoints(Specification<Point> spec, Pageable pageable);

  PointHistoryDTO addPointHistory(PointHistoryReqDTO pointHistoryReqDTO);

  void addPointsFromOrderReview(Review review);

  PointUserCurrentResDTO getCurrentUserPoints();

}

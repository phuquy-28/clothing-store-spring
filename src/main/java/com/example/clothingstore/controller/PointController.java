package com.example.clothingstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.PointHistoryReqDTO;
import com.example.clothingstore.dto.response.PointHistoryDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Point;
import com.example.clothingstore.entity.PointHistory;
import com.example.clothingstore.service.PointService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class PointController {

  private final Logger log = LoggerFactory.getLogger(PointController.class);

  private final PointService pointService;

  @GetMapping(UrlConfig.POINT + UrlConfig.USER_POINT)
  public ResponseEntity<ResultPaginationDTO> getUserPointHistory(
      @Filter Specification<PointHistory> spec, Pageable pageable) {
    return ResponseEntity.ok(pointService.getUserPointHistory(spec, pageable));
  }

  @GetMapping(UrlConfig.POINT)
  public ResponseEntity<ResultPaginationDTO> getUserPoint(@Filter Specification<Point> spec,
      Pageable pageable) {
    return ResponseEntity.ok(pointService.getPoints(spec, pageable));
  }

  @PostMapping(UrlConfig.POINT)
  public ResponseEntity<PointHistoryDTO> addPointHistory(
      @RequestBody @Valid PointHistoryReqDTO pointHistoryReqDTO) {
    log.debug("Request to {} {} for user {}", pointHistoryReqDTO.getActionType(),
        pointHistoryReqDTO.getPoints(), pointHistoryReqDTO.getEmailUser());
    return ResponseEntity.ok(pointService.addPointHistory(pointHistoryReqDTO));
  }
}

package com.example.clothingstore.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import com.example.clothingstore.dto.request.PromotionReqDTO;
import com.example.clothingstore.dto.response.PromotionResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Promotion;
import com.example.clothingstore.service.PromotionService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class PromotionController {

  private final Logger log = LoggerFactory.getLogger(PromotionController.class);

  private final PromotionService promotionService;

  @PostMapping(UrlConfig.PROMOTION)
  public ResponseEntity<PromotionResDTO> createPromotion(
      @Valid @RequestBody PromotionReqDTO promotionReqDTO) {
    log.debug("REST request to create promotion: {}", promotionReqDTO);
    return ResponseEntity.ok(promotionService.createPromotion(promotionReqDTO));
  }

  @PutMapping(UrlConfig.PROMOTION)
  public ResponseEntity<PromotionResDTO> updatePromotion(
      @Valid @RequestBody PromotionReqDTO promotionReqDTO) {
    log.debug("REST request to update promotion: {}", promotionReqDTO);
    return ResponseEntity.ok(promotionService.updatePromotion(promotionReqDTO));
  }

  @DeleteMapping(UrlConfig.PROMOTION + UrlConfig.ID)
  public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
    log.debug("REST request to delete promotion: {}", id);
    promotionService.deletePromotion(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping(UrlConfig.PROMOTION)
  public ResponseEntity<ResultPaginationDTO> getPromotions(
      @Filter Specification<Promotion> specification, Pageable pageable) {
    log.debug("REST request to get all promotions: {}, {}", specification, pageable);
    return ResponseEntity.ok(promotionService.getPromotions(specification, pageable));
  }

  @GetMapping(UrlConfig.PROMOTION + UrlConfig.ID)
  public ResponseEntity<PromotionResDTO> getPromotion(@PathVariable Long id) {
    log.debug("REST request to get promotion: {}", id);
    return ResponseEntity.ok(promotionService.getPromotion(id));
  }
}

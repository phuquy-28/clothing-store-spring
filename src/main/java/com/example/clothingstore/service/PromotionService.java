package com.example.clothingstore.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.dto.request.PromotionReqDTO;
import com.example.clothingstore.dto.response.PromotionResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Promotion;

public interface PromotionService {

  PromotionResDTO createPromotion(PromotionReqDTO promotionReqDTO);

  PromotionResDTO updatePromotion(PromotionReqDTO promotionReqDTO);

  void deletePromotion(Long id);

  ResultPaginationDTO getPromotions(Specification<Promotion> specification, Pageable pageable);

  PromotionResDTO getPromotion(Long id);
}

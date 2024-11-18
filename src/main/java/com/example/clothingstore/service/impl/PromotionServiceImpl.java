package com.example.clothingstore.service.impl;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Collections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.PromotionReqDTO;
import com.example.clothingstore.dto.response.PromotionResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO.Meta;
import com.example.clothingstore.entity.Category;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.Promotion;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.CategoryRepository;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.repository.PromotionRepository;
import com.example.clothingstore.service.CategoryService;
import com.example.clothingstore.service.ProductService;
import com.example.clothingstore.service.PromotionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

  private final PromotionRepository promotionRepository;

  private final ProductRepository productRepository;

  private final CategoryRepository categoryRepository;

  private final ProductService productService;

  private final CategoryService categoryService;

  @Override
  public PromotionResDTO createPromotion(PromotionReqDTO promotionReqDTO) {
    // validate list productIds if exist
    List<Product> products = null;
    if (promotionReqDTO.getProductIds() != null) {
      products = productRepository.findAllById(promotionReqDTO.getProductIds());
      if (products.size() != promotionReqDTO.getProductIds().size()) {
        throw new ResourceNotFoundException(ErrorMessage.PRODUCT_PROMOTION_NOT_FOUND);
      }
    }

    // validate list categoryIds if exist
    List<Category> categories = null;
    if (promotionReqDTO.getCategoryIds() != null) {
      categories = categoryRepository.findAllById(promotionReqDTO.getCategoryIds());
      if (categories.size() != promotionReqDTO.getCategoryIds().size()) {
        throw new ResourceNotFoundException(ErrorMessage.CATEGORY_PROMOTION_NOT_FOUND);
      }
    }

    Promotion promotion = new Promotion();
    promotion.setName(promotionReqDTO.getName());
    promotion.setDiscountRate(promotionReqDTO.getDiscountRate());
    promotion.setStartDate(promotionReqDTO.getStartDate().toInstant(ZoneOffset.UTC));
    promotion.setEndDate(promotionReqDTO.getEndDate().toInstant(ZoneOffset.UTC));
    promotion.setDescription(promotionReqDTO.getDescription());
    promotion.setProducts(products);
    promotion.setCategories(categories);
    Promotion savedPromotion = promotionRepository.save(promotion);

    return convertToPromotionResDTO(savedPromotion);
  }

  @Override
  public PromotionResDTO updatePromotion(PromotionReqDTO promotionReqDTO) {
    Promotion promotion = promotionRepository.findById(promotionReqDTO.getId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROMOTION_NOT_FOUND));

    promotion.setName(promotionReqDTO.getName());
    promotion.setDiscountRate(promotionReqDTO.getDiscountRate());
    promotion.setStartDate(promotionReqDTO.getStartDate().toInstant(ZoneOffset.UTC));
    promotion.setEndDate(promotionReqDTO.getEndDate().toInstant(ZoneOffset.UTC));
    promotion.setDescription(promotionReqDTO.getDescription());

    // validate list productIds if exist
    List<Product> products = null;
    if (promotionReqDTO.getProductIds() != null) {
      products = productRepository.findAllById(promotionReqDTO.getProductIds());
    }
    promotion.setProducts(products);

    // validate list categoryIds if exist
    List<Category> categories = null;
    if (promotionReqDTO.getCategoryIds() != null) {
      categories = categoryRepository.findAllById(promotionReqDTO.getCategoryIds());
    }
    promotion.setCategories(categories);

    Promotion updatedPromotion = promotionRepository.save(promotion);

    return convertToPromotionResDTO(updatedPromotion);
  }

  @Override
  public void deletePromotion(Long id) {
    Promotion promotion = promotionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROMOTION_NOT_FOUND));
    promotionRepository.delete(promotion);
  }

  @Override
  public ResultPaginationDTO getPromotions(Specification<Promotion> specification,
      Pageable pageable) {
    Page<Promotion> promotions = promotionRepository.findAll(specification, pageable);
    List<PromotionResDTO> promotionResDTOs = promotions.getContent().stream()
        .map(promotion -> convertToPromotionResDTO(promotion)).collect(Collectors.toList());
    return ResultPaginationDTO.builder()
        .meta(Meta.builder().page((long) promotions.getNumber())
            .pageSize((long) promotions.getSize()).pages((long) promotions.getTotalPages())
            .total(promotions.getTotalElements()).build())
        .data(promotionResDTOs).build();
  }

  @Override
  public PromotionResDTO getPromotion(Long id) {
    Promotion promotion = promotionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROMOTION_NOT_FOUND));
    return convertToPromotionResDTO(promotion);
  }

  private PromotionResDTO convertToPromotionResDTO(Promotion promotion) {
    return PromotionResDTO.builder().id(promotion.getId()).name(promotion.getName())
        .discountRate(promotion.getDiscountRate())
        .startDate(promotion.getStartDate().atOffset(ZoneOffset.UTC).toLocalDateTime())
        .endDate(promotion.getEndDate().atOffset(ZoneOffset.UTC).toLocalDateTime())
        .description(promotion.getDescription())
        .products(Optional.ofNullable(promotion.getProducts())
            .map(list -> list.stream().map(productService::convertToProductResDTO)
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList()))
        .categories(Optional
            .ofNullable(promotion.getCategories()).map(list -> list.stream()
                .map(categoryService::convertToCategoryResDTO).collect(Collectors.toList()))
            .orElse(Collections.emptyList()))
        .build();
  }

}

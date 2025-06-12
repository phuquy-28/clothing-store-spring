package com.example.clothingstore.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.ProductReqDTO;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.service.ProductService;
import com.example.clothingstore.service.RecommendationService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class ProductController {

  private final Logger log = LoggerFactory.getLogger(ProductController.class);

  private final ProductService productService;

  private final UserService userService;

  private final RecommendationService recommendationService;

  @PostMapping(UrlConfig.PRODUCT + UrlConfig.UPLOAD_IMAGES)
  public ResponseEntity<UploadImageResDTO> uploadImages(
      @RequestBody UploadImageReqDTO uploadImageReqDTO) {
    log.debug("Upload images request: {}", uploadImageReqDTO);
    UploadImageResDTO uploadImageResDTO = productService.createSignedUrl(uploadImageReqDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(uploadImageResDTO);
  }

  @PostMapping(UrlConfig.PRODUCT)
  public ResponseEntity<ProductResDTO> createProduct(
      @RequestBody @Valid ProductReqDTO productReqDTO) {
    log.debug("Create product request: {}", productReqDTO);
    ProductResDTO productResDTO = productService.createProduct(productReqDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(productResDTO);
  }

  @GetMapping(UrlConfig.PRODUCT + UrlConfig.PRODUCT_ID + UrlConfig.ID)
  public ResponseEntity<ProductResDTO> getProductById(@PathVariable Long id) {
    log.debug("Get product by id request: {}", id);
    ProductResDTO productResDTO = productService.getProductById(id);
    return ResponseEntity.status(HttpStatus.OK).body(productResDTO);
  }

  @GetMapping(UrlConfig.PRODUCT + UrlConfig.PRODUCT_SLUG)
  public ResponseEntity<ProductResDTO> getProductBySlug(@PathVariable String slug) {
    log.debug("Get product by slug request: {}", slug);
    ProductResDTO productResDTO = productService.getProductBySlug(slug);
    return ResponseEntity.status(HttpStatus.OK).body(productResDTO);
  }

  // @GetMapping(UrlConfig.PRODUCT)
  // public ResponseEntity<ResultPaginationDTO> getProducts(
  // @RequestParam(required = false) Boolean isBestSeller,
  // @RequestParam(required = false) Boolean isDiscounted,
  // @RequestParam(required = false) Integer days, @Filter Specification<Product> specification,
  // Pageable pageable, HttpServletRequest request) {

  // boolean hasBestSellerParam = request.getParameterMap().containsKey("isBestSeller");
  // boolean hasDiscountedParam = request.getParameterMap().containsKey("isDiscounted");

  // log.debug(
  // "Get products request: hasBestSellerParam={}, hasDiscountedParam={}, days={}, spec={},
  // pageable={}",
  // hasBestSellerParam, hasDiscountedParam, days, specification, pageable);

  // if (hasBestSellerParam) {
  // return ResponseEntity.ok(productService.getBestSellerProducts(days, pageable));
  // }

  // if (hasDiscountedParam) {
  // return ResponseEntity.ok(productService.getDiscountedProducts(pageable));
  // }

  // return ResponseEntity.ok(productService.getProducts(specification, pageable));
  // }

  @GetMapping(UrlConfig.PRODUCT)
  public ResponseEntity<ResultPaginationDTO> getProducts(
      @RequestParam(required = false, defaultValue = "false") Boolean isBestSeller,
      @RequestParam(required = false, defaultValue = "false") Boolean isDiscounted,
      @RequestParam(required = false) Integer days,
      @RequestParam(required = false) Double averageRating,
      @RequestParam(required = false, defaultValue = "false") Boolean hasDiscount,
      @RequestParam(required = false) Double minPrice,
      @RequestParam(required = false) Double maxPrice, @RequestParam(required = false) String sizes,
      @RequestParam(required = false) String sortField,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder,
      @Filter Specification<Product> specification, Pageable pageable) {
    return ResponseEntity
        .ok(productService.getProducts(isBestSeller, isDiscounted, days, averageRating, hasDiscount,
            minPrice, maxPrice, sizes, sortField, sortOrder, specification, pageable));
  }

  @PutMapping(UrlConfig.PRODUCT)
  public ResponseEntity<ProductResDTO> updateProduct(
      @RequestBody @Valid ProductReqDTO productReqDTO) {
    log.debug("Update product request: {}", productReqDTO);
    ProductResDTO productResDTO = productService.updateProduct(productReqDTO);
    return ResponseEntity.status(HttpStatus.OK).body(productResDTO);
  }

  @DeleteMapping(UrlConfig.PRODUCT + UrlConfig.ID)
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    log.debug("Delete product request: {}", id);
    productService.deleteProduct(id);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping(UrlConfig.PRODUCT + UrlConfig.PRODUCT_SLUG + UrlConfig.REVIEW)
  public ResponseEntity<ResultPaginationDTO> getReviewsByProductSlug(@PathVariable String slug,
      @RequestParam(required = false) Integer rating, Pageable pageable) {
    log.debug("Get reviews by product slug request: {}", slug);
    ResultPaginationDTO resultPaginationDTO =
        productService.getReviewsByProductSlug(slug, rating, pageable);
    return ResponseEntity.status(HttpStatus.OK).body(resultPaginationDTO);
  }

  @GetMapping(UrlConfig.PRODUCT + UrlConfig.RECOMMENDATIONS + UrlConfig.FOR_YOU)
  public ResponseEntity<List<ProductResDTO>> getRecommendationsForUser(
      @RequestParam(defaultValue = "10") int limit) {
    log.debug("REST request to get personalized recommendations for current user.");
    User currentUser =
        userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin().orElse(null));
    List<ProductResDTO> recommendations =
        recommendationService.getRecommendationsForUser(currentUser, limit);
    return ResponseEntity.ok(recommendations);
  }

  @PostMapping(UrlConfig.PRODUCT + UrlConfig.ID + UrlConfig.LOG_VIEW)
  public ResponseEntity<Void> logProductView(@PathVariable Long id) {
    User currentUser = userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin()
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_LOGGED_IN)));
    productService.logUserProductView(currentUser.getId(), id);
    return ResponseEntity.ok().build();
  }
}

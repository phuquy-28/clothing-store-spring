package com.example.clothingstore.controller;

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
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.ProductReqDTO;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.service.ProductService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class ProductController {

  private final Logger log = LoggerFactory.getLogger(ProductController.class);

  private final ProductService productService;

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

  @GetMapping(UrlConfig.PRODUCT)
  public ResponseEntity<ResultPaginationDTO> getProducts(
      @Filter Specification<Product> specification, Pageable pageable) {
    log.debug("Get products request: {}, {}", specification, pageable);
    return ResponseEntity.status(HttpStatus.OK)
        .body(productService.getProducts(specification, pageable));
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
}

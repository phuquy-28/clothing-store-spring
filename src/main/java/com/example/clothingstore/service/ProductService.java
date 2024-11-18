package com.example.clothingstore.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.dto.request.ProductReqDTO;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.entity.Product;

public interface ProductService {
  UploadImageResDTO createSignedUrl(UploadImageReqDTO uploadImageReqDTO);

  ProductResDTO createProduct(ProductReqDTO productReqDTO);

  ProductResDTO getProductBySlug(String slug);

  ResultPaginationDTO getProducts(Specification<Product> specification, Pageable pageable);

  ProductResDTO updateProduct(ProductReqDTO productReqDTO);

  void deleteProduct(Long id);

  ProductResDTO getProductById(Long id);

  ResultPaginationDTO getBestSellerProducts(Integer days, Pageable pageable);

  ProductResDTO convertToProductResDTO(Product product);

  ResultPaginationDTO getDiscountedProducts(Pageable pageable);
}

package com.example.clothingstore.service;

import com.example.clothingstore.dto.response.ProductImportResponseDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.ProductVariant;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

public interface InventoryService {
  ResultPaginationDTO listInventory(Specification<ProductVariant> specification, Pageable pageable);

  Resource exportInventoryTemplate();

  ProductImportResponseDTO importInventoryUpdate(MultipartFile file);

  void updateInventory(String sku, Long quantity, String note);

}

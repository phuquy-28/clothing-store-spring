package com.example.clothingstore.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import com.example.clothingstore.dto.response.ProductImportResponseDTO;
import com.example.clothingstore.enumeration.ImportMode;
import com.example.clothingstore.enumeration.TemplateType;

public interface ImportService {
  Resource getImportTemplateFile(TemplateType templateType);

  ProductImportResponseDTO importProducts(MultipartFile file, ImportMode addOnly);

  ProductImportResponseDTO importCategories(MultipartFile file, ImportMode importMode);

}

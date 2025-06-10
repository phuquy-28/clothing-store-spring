package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImportResponseDTO {
  private int totalRowsRead;
  private int successfulImports;
  private String successMessage;
}

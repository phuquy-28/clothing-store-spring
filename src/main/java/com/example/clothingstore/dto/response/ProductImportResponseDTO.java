package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProductImportResponseDTO {
  private int totalRowsRead;
  private int successfulImports;
  private int failedImports;
  private List<String> errorMessages;
  private String successMessage;
}

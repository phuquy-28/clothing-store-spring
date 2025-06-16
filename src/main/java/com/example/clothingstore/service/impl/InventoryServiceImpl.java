package com.example.clothingstore.service.impl;

import com.example.clothingstore.annotation.EnableSoftDeleteFilter;
import com.example.clothingstore.config.Translator;
import com.example.clothingstore.dto.response.InventoryVariantDTO;
import com.example.clothingstore.dto.response.ProductImportResponseDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.InventoryHistory;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.enumeration.InventoryChangeType;
import com.example.clothingstore.exception.DataValidationException;
import com.example.clothingstore.repository.InventoryHistoryRepository;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.repository.ProductVariantRepository;
import com.example.clothingstore.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

  private final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
  private final ProductVariantRepository variantRepository;
  private final ProductRepository productRepository;
  private final InventoryHistoryRepository inventoryHistoryRepository;

  @EnableSoftDeleteFilter
  @Override
  public ResultPaginationDTO listInventory(Specification<ProductVariant> specification,
      Pageable pageable) {
    Page<ProductVariant> variantPage = variantRepository.findAll(specification, pageable);

    List<InventoryVariantDTO> dtoList =
        variantPage.getContent().stream().map(this::mapToInventoryDTO).collect(Collectors.toList());

    ResultPaginationDTO.Meta meta = ResultPaginationDTO.Meta.builder()
        .page((long) variantPage.getNumber()).pageSize((long) variantPage.getSize())
        .pages((long) variantPage.getTotalPages()).total(variantPage.getTotalElements()).build();

    return ResultPaginationDTO.builder().meta(meta).data(dtoList).build();
  }

  private InventoryVariantDTO mapToInventoryDTO(ProductVariant variant) {
    long quantitySold = productRepository.countSoldQuantityByVariantId(variant.getId());
    return InventoryVariantDTO.builder().variantId(variant.getId()).sku(variant.getSku())
        .productId(variant.getProduct().getId()).productName(variant.getProduct().getName())
        .color(variant.getColor().name()).size(variant.getSize().name())
        .quantityInStock(variant.getQuantity()).quantitySold(quantitySold)
        .variantImage(variant.getImages().get(0).getPublicUrl()).build();
  }

  @Override
  public Resource exportInventoryTemplate() {
    List<ProductVariant> variants = variantRepository.findAll();

    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Danh sách sản phẩm");

      // Header Row
      Row headerRow = sheet.createRow(0);
      String[] headers =
          {"SKU", "Tên sản phẩm", "Màu", "Kích thước", "Số lượng hiện có", "Ghi chú"};
      for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
      }

      // Data Rows
      int rowIdx = 1;
      for (ProductVariant variant : variants) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(variant.getSku());
        row.createCell(1).setCellValue(variant.getProduct().getName());
        row.createCell(2).setCellValue(variant.getColor().name());
        row.createCell(3).setCellValue(variant.getSize().name());
        row.createCell(4).setCellValue(variant.getQuantity());
      }

      workbook.write(out);
      return new ByteArrayResource(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Failed to export inventory data: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public ProductImportResponseDTO importInventoryUpdate(MultipartFile file) {
    int updatedCount = 0;
    int totalRows = 0;

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      if (sheet == null) {
        throw new DataValidationException("import.sheet.not_found", "Danh sách sản phẩm");
      }

      Iterator<Row> rowIterator = sheet.iterator();
      if (rowIterator.hasNext()) {
        rowIterator.next(); // Skip header
        totalRows++;
      }

      while (rowIterator.hasNext()) {
        Row currentRow = rowIterator.next();
        int currentRowNum = currentRow.getRowNum() + 1;
        totalRows++;

        try {
          Cell skuCell = currentRow.getCell(0);
          Cell quantityCell = currentRow.getCell(4);
          Cell notesCell = currentRow.getCell(5);

          // Kiểm tra SKU
          if (skuCell == null || skuCell.getCellType() != CellType.STRING
              || !StringUtils.hasText(skuCell.getStringCellValue())) {
            throw new DataValidationException("import.error.row.col.blank",
                String.valueOf(currentRowNum), "SKU");
          }
          String sku = skuCell.getStringCellValue().trim();

          // Kiểm tra số lượng
          if (quantityCell == null || quantityCell.getCellType() != CellType.NUMERIC) {
            throw new DataValidationException("import.error.row.col.expected_numeric",
                String.valueOf(currentRowNum), "Số lượng");
          }

          String notes = notesCell != null ? notesCell.getStringCellValue().trim() : "";

          int newQuantity;
          try {
            newQuantity = (int) quantityCell.getNumericCellValue();
            if (newQuantity < 0) {
              throw new DataValidationException("import.error.row.col.invalid_quantity",
                  String.valueOf(currentRowNum), "Số lượng");
            }
          } catch (IllegalStateException e) {
            throw new DataValidationException("import.error.row.col.invalid_number_format",
                String.valueOf(currentRowNum), quantityCell.toString());
          }

          // Kiểm tra sự tồn tại của variant
          ProductVariant variant = variantRepository.findBySku(sku)
              .orElseThrow(() -> new DataValidationException("import.error.row.col.sku_not_found",
                  String.valueOf(currentRowNum), sku));

          int oldQuantity = variant.getQuantity();

          // Chỉ cập nhật và ghi lại lịch sử nếu có sự thay đổi số lượng
          if (oldQuantity != newQuantity) {
            // Cập nhật số lượng
            variant.setQuantity(newQuantity);
            variantRepository.save(variant);

            // Ghi lại lịch sử
            InventoryHistory history = new InventoryHistory();
            history.setProductVariant(variant);
            history.setChangeInQuantity(newQuantity - oldQuantity);
            history.setQuantityAfterChange(newQuantity);
            history.setTypeOfChange(InventoryChangeType.EXCEL_IMPORT);
            history.setTimestamp(Instant.now());
            history.setNotes("Cập nhật qua Excel từ file: " + file.getOriginalFilename()
                + (notes.isEmpty() ? "" : " với ghi chú: " + notes));
            inventoryHistoryRepository.save(history);
            updatedCount++;
          }

        } catch (DataValidationException e) {
          String errorMessage = Translator.toLocale(e.getMessageKey(),
              e.getArgs() != null ? e.getArgs() : new Object[] {});
          log.error("Lỗi xác thực ở dòng {}: {}", currentRowNum, errorMessage);
        } catch (Exception e) {
          log.error("Lỗi không xác định khi xử lý dòng {}: {}", currentRowNum, e.getMessage(), e);
        }
      }

      if (updatedCount == 0) {
        if (totalRows > 1) {
          throw new DataValidationException("import.error.no_valid_inventory_updates");
        } else {
          throw new DataValidationException("import.error.empty_file_no_data");
        }
      }

    } catch (IOException e) {
      log.error("Lỗi khi đọc file Excel '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
      throw new RuntimeException(Translator.toLocale("excel.read.error"), e);
    }

    return ProductImportResponseDTO.builder().totalRowsRead(totalRows - 1)
        .successfulImports(updatedCount)
        .successMessage(Translator.toLocale("import.inventory.success.total", updatedCount))
        .build();
  }

  @Override
  public void updateInventory(String sku, Long quantity, String note) {
    ProductVariant variant = variantRepository.findBySku(sku)
        .orElseThrow(() -> new DataValidationException("inventory.sku.not.found", sku));

    int oldQuantity = variant.getQuantity();

    // Chỉ cập nhật và ghi lại lịch sử nếu có sự thay đổi số lượng
    if (oldQuantity != quantity.intValue()) {
      variant.setQuantity(quantity.intValue());
      variantRepository.save(variant);

      InventoryHistory history = new InventoryHistory();
      history.setProductVariant(variant);
      history.setChangeInQuantity(quantity.intValue() - oldQuantity);
      history.setQuantityAfterChange(quantity.intValue());
      history.setTypeOfChange(InventoryChangeType.MANUAL_UPDATE);
      history.setTimestamp(Instant.now());
      history.setNotes(
          "Cập nhật tồn kho" + (note != null && !note.isEmpty() ? " với ghi chú: " + note : ""));
      inventoryHistoryRepository.save(history);
    }
  }
}

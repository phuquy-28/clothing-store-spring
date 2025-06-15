package com.example.clothingstore.service.impl;

import com.example.clothingstore.dto.response.InventoryHistoryDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.InventoryHistory;
import com.example.clothingstore.repository.InventoryHistoryRepository;
import com.example.clothingstore.service.InventoryHistoryService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryHistoryServiceImpl implements InventoryHistoryService {

  private final InventoryHistoryRepository inventoryHistoryRepository;

  @Override
  public ResultPaginationDTO getAllInventoryHistory(Specification<InventoryHistory> spec,
      Pageable pageable) {
    // Ensure default sorting by productSku and timestamp DESC if no sort is specified
    if (pageable.getSort().isEmpty()) {
      pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
          Sort.by(Sort.Direction.ASC, "productVariant.sku")
              .and(Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    Page<InventoryHistory> page = inventoryHistoryRepository.findAll(spec, pageable);

    // Convert to DTOs and sort
    List<InventoryHistoryDTO> sortedData = page.getContent().stream().map(this::toDto)
        .sorted(Comparator.comparing(InventoryHistoryDTO::getProductSku)
            .thenComparing(InventoryHistoryDTO::getTimestamp, Comparator.reverseOrder()))
        .collect(Collectors.toList());

    return ResultPaginationDTO.builder()
        .meta(ResultPaginationDTO.Meta.builder().page(Long.valueOf(page.getNumber()))
            .pageSize(Long.valueOf(page.getSize())).pages(Long.valueOf(page.getTotalPages()))
            .total(page.getTotalElements()).build())
        .data(sortedData).build();
  }

  @Override
  public Resource exportInventoryHistory(Specification<InventoryHistory> spec) {
    // Lấy tất cả dữ liệu và sắp xếp
    List<InventoryHistory> histories = inventoryHistoryRepository.findAll(spec,
        Sort.by("productVariant.sku").ascending().and(Sort.by("timestamp").descending()));

    // Chuyển đổi thành DTO và sắp xếp
    List<InventoryHistoryDTO> sortedData = histories.stream().map(this::toDto)
        .sorted(Comparator.comparing(InventoryHistoryDTO::getProductSku)
            .thenComparing(InventoryHistoryDTO::getTimestamp, Comparator.reverseOrder()))
        .collect(Collectors.toList());

    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Lịch sử tồn kho");

      // Tạo header
      Row headerRow = sheet.createRow(0);
      String[] headers = {"SKU", "Tên sản phẩm", "Thay đổi số lượng", "Số lượng sau thay đổi",
          "Loại thay đổi", "Thời gian", "Ghi chú", "Người cập nhật"};

      for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
      }

      // Điền dữ liệu
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      int rowIdx = 1;
      for (InventoryHistoryDTO history : sortedData) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(history.getProductSku());
        row.createCell(1).setCellValue(history.getProductName());
        row.createCell(2).setCellValue(history.getChangeInQuantity());
        row.createCell(3).setCellValue(history.getQuantityAfterChange());
        row.createCell(4).setCellValue(history.getTypeOfChange().toString());
        row.createCell(5).setCellValue(dateFormat.format(Date.from(history.getTimestamp())));
        row.createCell(6).setCellValue(history.getNotes() != null ? history.getNotes() : "");
        row.createCell(7)
            .setCellValue(history.getUpdatedBy() != null ? history.getUpdatedBy() : "");
      }

      // Tự động điều chỉnh độ rộng cột
      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayResource(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Lỗi khi tạo file Excel: " + e.getMessage());
    }
  }

  private InventoryHistoryDTO toDto(InventoryHistory entity) {
    return InventoryHistoryDTO.builder().id(entity.getId())
        .productSku(entity.getProductVariant().getSku())
        .productName(entity.getProductVariant().getProduct().getName())
        .changeInQuantity(entity.getChangeInQuantity())
        .quantityAfterChange(entity.getQuantityAfterChange()).typeOfChange(entity.getTypeOfChange())
        .timestamp(entity.getTimestamp())
        .orderCode(entity.getOrder() != null ? entity.getOrder().getCode() : null)
        .notes(entity.getNotes()).updatedBy(entity.getCreatedBy()).build();
  }
}

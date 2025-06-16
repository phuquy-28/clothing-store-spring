package com.example.clothingstore.service.impl;

import com.example.clothingstore.dto.response.InventoryHistoryDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.InventoryHistory;
import com.example.clothingstore.repository.InventoryHistoryRepository;
import com.example.clothingstore.service.InventoryHistoryService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
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
import java.time.LocalDate;
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
  public Resource exportInventoryHistory(Specification<InventoryHistory> spec, String sku,
      String productName, LocalDate startDate, LocalDate endDate) {
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

      int rowIdx = 0;
      Row titleRow = sheet.createRow(rowIdx++);
      StringBuilder title = new StringBuilder("Lịch sử tồn kho");

      if (sku != null && !sku.isEmpty()) {
        title.append(" của mã sản phẩm ").append(sku);
      }

      if (startDate != null || endDate != null) {
        title.append(" (");
        if (startDate != null) {
          title.append("từ ngày ")
              .append(startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        if (endDate != null) {
          if (startDate != null) {
            title.append(" ");
          }
          title.append("đến ngày ")
              .append(endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        title.append(")");
      }

      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue(title.toString());

      // Tạo style cho tiêu đề
      CellStyle titleStyle = workbook.createCellStyle();
      titleStyle.setAlignment(HorizontalAlignment.CENTER);
      titleCell.setCellStyle(titleStyle);

      // Merge các ô của dòng tiêu đề
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

      // Thêm dòng trống để phân cách
      rowIdx++;

      // Tạo header cho dữ liệu
      Row headerRow = sheet.createRow(rowIdx++);
      String[] headers = {"Thời gian", "SKU", "Thay đổi số lượng", "Số lượng sau thay đổi",
          "Ghi chú", "User thực hiện"};

      for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
      }

      // Điền dữ liệu
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
      for (InventoryHistoryDTO history : sortedData) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(dateFormat.format(Date.from(history.getTimestamp())));
        row.createCell(1).setCellValue(history.getProductSku());
        row.createCell(2).setCellValue(history.getChangeInQuantity());
        row.createCell(3).setCellValue(history.getQuantityAfterChange());
        row.createCell(4).setCellValue(history.getNotes() != null ? history.getNotes() : "");
        row.createCell(5)
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

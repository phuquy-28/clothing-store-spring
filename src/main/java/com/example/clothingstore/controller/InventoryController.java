// Create new file: src/main/java/com/example/clothingstore/controller/InventoryController.java
package com.example.clothingstore.controller;

import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.UpdateInventoryReq;
import com.example.clothingstore.dto.response.ProductImportResponseDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.InventoryHistory;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.service.InventoryService;
import com.example.clothingstore.service.InventoryHistoryService;
import com.example.clothingstore.specification.InventoryHistorySpecification;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryService inventoryService;
  private final InventoryHistoryService inventoryHistoryService;

  @GetMapping(UrlConfig.INVENTORY)
  public ResponseEntity<ResultPaginationDTO> getInventoryList(
      @Filter Specification<ProductVariant> specification, Pageable pageable) {
    return ResponseEntity.ok(inventoryService.listInventory(specification, pageable));
  }

  @GetMapping(UrlConfig.INVENTORY + UrlConfig.EXPORT)
  public ResponseEntity<Resource> exportInventory() {
    String timestamp = new SimpleDateFormat("dd_MM_yyyy_HH_mm").format(new Date());
    String filename = "inventory_export_" + timestamp + ".xlsx";
    Resource file = inventoryService.exportInventoryTemplate();

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType
            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(file);
  }

  @PostMapping(value = UrlConfig.INVENTORY + UrlConfig.IMPORT,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ProductImportResponseDTO> importInventory(
      @RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(inventoryService.importInventoryUpdate(file));
  }

  @PutMapping(UrlConfig.INVENTORY)
  public ResponseEntity<Void> updateInventory(
      @RequestBody @Valid UpdateInventoryReq updateInventoryReq) {
    inventoryService.updateInventory(updateInventoryReq.getSku(), updateInventoryReq.getQuantity());
    return ResponseEntity.ok().build();
  }

  @GetMapping(UrlConfig.INVENTORY + UrlConfig.HISTORY)
  public ResponseEntity<ResultPaginationDTO> getInventoryHistory(
      @RequestParam(required = false) String sku,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @PageableDefault(sort = {"productVariant.sku", "timestamp"},
          direction = Sort.Direction.DESC) Pageable pageable) {

    Instant startInstant =
        (startDate != null) ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
    Instant endInstant =
        (endDate != null) ? endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

    Specification<InventoryHistory> spec =
        Specification.where(InventoryHistorySpecification.hasProductSku(sku))
            .and(InventoryHistorySpecification.hasTimestampAfter(startInstant))
            .and(InventoryHistorySpecification.hasTimestampBefore(endInstant));

    return ResponseEntity.ok(inventoryHistoryService.getAllInventoryHistory(spec, pageable));
  }

  @GetMapping(UrlConfig.INVENTORY + UrlConfig.HISTORY + UrlConfig.EXPORT)
  public ResponseEntity<Resource> exportInventoryHistory(@RequestParam(required = false) String sku,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {

    Instant startInstant =
        (startDate != null) ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
    Instant endInstant =
        (endDate != null) ? endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

    Specification<InventoryHistory> spec =
        Specification.where(InventoryHistorySpecification.hasProductSku(sku))
            .and(InventoryHistorySpecification.hasTimestampAfter(startInstant))
            .and(InventoryHistorySpecification.hasTimestampBefore(endInstant));

    String timestamp = new SimpleDateFormat("dd_MM_yyyy_HH_mm").format(new Date());
    String filename = "inventory_history_export_" + timestamp + ".xlsx";
    Resource file = inventoryHistoryService.exportInventoryHistory(spec);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType
            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(file);
  }
}

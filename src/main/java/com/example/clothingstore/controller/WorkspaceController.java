package com.example.clothingstore.controller;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.DashboardResDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.ProductImportResponseDTO;
import com.example.clothingstore.dto.response.RevenueByMonth;
import com.example.clothingstore.enumeration.ImportMode;
import com.example.clothingstore.enumeration.TemplateType;
import com.example.clothingstore.service.ImportService;
import com.example.clothingstore.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class WorkspaceController {

  private final WorkspaceService workspaceService;
  private final ImportService importService;

  @PostMapping(UrlConfig.WORKSPACE + UrlConfig.LOGIN)
  public ResponseEntity<LoginResDTO> login(@RequestBody @Valid LoginReqDTO loginReqDTO) {
    return ResponseEntity.ok(workspaceService.login(loginReqDTO));
  }

  @GetMapping(UrlConfig.WORKSPACE + UrlConfig.DASHBOARD)
  public ResponseEntity<DashboardResDTO> getDashboard() {
    return ResponseEntity.ok(workspaceService.getDashboard());
  }

  @GetMapping(UrlConfig.WORKSPACE + UrlConfig.REVENUE_BY_MONTH)
  public ResponseEntity<RevenueByMonth> getRevenueByMonth(
      @RequestParam(required = false) Long year) {
    return ResponseEntity.ok(workspaceService.getRevenueByMonth(year));
  }

  @GetMapping(UrlConfig.WORKSPACE + UrlConfig.IMPORT_TEMPLATE)
  public ResponseEntity<Resource> downloadImportTemplate(
      @PathVariable("templateType") String templateTypeString) throws IOException {
    TemplateType templateType = TemplateType.fromTypeName(templateTypeString.toLowerCase());
    Resource template = importService.getImportTemplateFile(templateType);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + templateType.getFileName());
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    return ResponseEntity.ok().headers(headers).contentLength(template.contentLength())
        .body(template);
  }

  @PostMapping(value = UrlConfig.WORKSPACE + UrlConfig.IMPORT_PRODUCTS,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ProductImportResponseDTO> importProducts(
      @RequestParam("file") MultipartFile file) {
    ProductImportResponseDTO response = importService.importProducts(file, ImportMode.ADD_ONLY);
    return ResponseEntity.ok(response);
  }
}

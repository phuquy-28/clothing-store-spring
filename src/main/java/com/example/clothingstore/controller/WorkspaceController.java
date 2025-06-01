package com.example.clothingstore.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.clothingstore.constant.AppConstant;
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
import com.example.clothingstore.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class WorkspaceController {

  private final Logger log = LoggerFactory.getLogger(WorkspaceController.class);
  private final WorkspaceService workspaceService;
  private final ImportService importService;
  private final SecurityUtil securityUtil;

  @Value("${spring.application.domain}")
  private final String DOMAIN;

  @PostMapping(UrlConfig.WORKSPACE + UrlConfig.LOGIN)
  public ResponseEntity<LoginResDTO> login(@RequestBody @Valid LoginReqDTO loginReqDTO) {
    LoginResDTO loginResDTO = workspaceService.login(loginReqDTO);

    // Create refresh token
    String refreshToken = securityUtil.createRefreshToken(loginReqDTO.getEmail(), loginResDTO);

    // Create cookie
    ResponseCookie springCookie =
        ResponseCookie.from(AppConstant.REFRESH_TOKEN_COOKIE_NAME, refreshToken).httpOnly(true)
            .secure(true).path("/").maxAge(AppConstant.REFRESH_TOKEN_COOKIE_EXPIRE).sameSite("None")
            .domain(DOMAIN).build();

    // Return response
    log.debug("Set refresh token cookie: {}", springCookie.toString());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
        .body(loginResDTO);
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

  @PostMapping(value = UrlConfig.WORKSPACE + UrlConfig.IMPORT_CATEGORIES,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ProductImportResponseDTO> importCategories(
      @RequestParam("file") MultipartFile file) {
    ProductImportResponseDTO response = importService.importCategories(file, ImportMode.ADD_ONLY);
    return ResponseEntity.ok(response);
  }
}

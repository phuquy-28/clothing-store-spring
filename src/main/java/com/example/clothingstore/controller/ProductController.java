package com.example.clothingstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.service.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class ProductController {

  private final Logger log = LoggerFactory.getLogger(ProductController.class);

  private final ProductService productService;

  @PostMapping(UrlConfig.PRODUCT + UrlConfig.UPLOAD_IMAGES)
  public ResponseEntity<UploadImageResDTO> uploadImages(
      @RequestBody UploadImageReqDTO uploadImageReqDTO) {
    log.debug("REST request to get signed URLs for image upload: {}", uploadImageReqDTO);
    UploadImageResDTO uploadImageResDTO = productService.createSignedUrl(uploadImageReqDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(uploadImageResDTO);
  }
}

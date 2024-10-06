package com.example.clothingstore.service.impl;

import org.springframework.stereotype.Service;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.service.CloudStorageService;
import com.example.clothingstore.service.ProductService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final CloudStorageService cloudStorageService;

  @Override
  public UploadImageResDTO createSignedUrl(UploadImageReqDTO uploadImageReqDTO) {
    return cloudStorageService.createSignedUrl(uploadImageReqDTO);
  }

}

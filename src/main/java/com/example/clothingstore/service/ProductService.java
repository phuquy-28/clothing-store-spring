package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;

public interface ProductService {
  UploadImageResDTO createSignedUrl(UploadImageReqDTO uploadImageReqDTO);
}

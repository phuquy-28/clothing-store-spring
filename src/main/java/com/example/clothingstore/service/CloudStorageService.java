package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.request.MultiMediaUploadReqDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.dto.response.MultiMediaUploadResDTO;

public interface CloudStorageService {

  UploadImageResDTO createSignedUrl(UploadImageReqDTO uploadImageReqDTO);

  UploadImageResDTO createSignedUrlWithDirectory(UploadImageReqDTO uploadImageReqDTO,
      String directory);

  MultiMediaUploadResDTO createMultiMediaSignedUrlsWithDirectory(
      MultiMediaUploadReqDTO uploadRequestDTO, String directory);
}

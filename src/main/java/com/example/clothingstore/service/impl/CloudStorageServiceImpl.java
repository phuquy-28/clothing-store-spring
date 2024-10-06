package com.example.clothingstore.service.impl;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.service.CloudStorageService;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.annotation.PostConstruct;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.HttpMethod;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;

@Service
@RequiredArgsConstructor
public class CloudStorageServiceImpl implements CloudStorageService {

  @Value("${google.cloud.storage.project-id}")
  private String projectId;

  @Value("${google.cloud.storage.bucket}")
  private String bucket;

  @Value("${google.cloud.credentials.path}")
  private String credentialsPath;

  private final Logger logger = LoggerFactory.getLogger(CloudStorageServiceImpl.class);

  private Storage storage;

  @PostConstruct
  public void initialize() throws IOException {
    try {
      GoogleCredentials credentials = GoogleCredentials.fromStream(
          new ClassPathResource(credentialsPath).getInputStream());
      this.storage = StorageOptions.newBuilder()
          .setCredentials(credentials)
          .setProjectId(projectId)
          .build()
          .getService();
    } catch (IOException e) {
      logger.error("Failed to initialize Google Cloud Storage: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public UploadImageResDTO createSignedUrl(UploadImageReqDTO uploadImageReqDTO) {
    UploadImageResDTO response = new UploadImageResDTO();

    if (uploadImageReqDTO.getFileName1() != null) {
      response.setSignedUrl1(generateSignedUrl(uploadImageReqDTO.getFileName1()));
    }
    if (uploadImageReqDTO.getFileName2() != null) {
      response.setSignedUrl2(generateSignedUrl(uploadImageReqDTO.getFileName2()));
    }
    if (uploadImageReqDTO.getFileName3() != null) {
      response.setSignedUrl3(generateSignedUrl(uploadImageReqDTO.getFileName3()));
    }
    if (uploadImageReqDTO.getFileName4() != null) {
      response.setSignedUrl4(generateSignedUrl(uploadImageReqDTO.getFileName4()));
    }

    return response;
  }

  private String generateSignedUrl(String fileName) {
    try {
      BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, fileName)).build();

      URL url = storage.signUrl(blobInfo, 10, TimeUnit.MINUTES,
          Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
          Storage.SignUrlOption.withV4Signature());
      logger.debug("Generated signed URL for {}: {}", fileName, url.toString());
      return url.toString();
    } catch (StorageException e) {
      e.printStackTrace();
      logger.error("Error generating signed URL for {}: {}", fileName, e.getMessage());
      return null;
    }
  }

}

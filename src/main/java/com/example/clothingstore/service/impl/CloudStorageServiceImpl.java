package com.example.clothingstore.service.impl;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.request.MultiMediaUploadReqDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.dto.response.MultiMediaUploadResDTO;
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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import com.example.clothingstore.exception.InvalidFileTypeException;

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

  private static final List<String> ALLOWED_EXTENSIONS =
      Arrays.asList("png", "jpg", "jpeg", "webp", "mp4", "mov");

  @PostConstruct
  public void initialize() throws IOException {
    try {
      GoogleCredentials credentials =
          GoogleCredentials.fromStream(new ClassPathResource(credentialsPath).getInputStream());
      this.storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId)
          .build().getService();
    } catch (IOException e) {
      logger.error("Failed to initialize Google Cloud Storage: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public UploadImageResDTO createSignedUrl(UploadImageReqDTO uploadImageReqDTO) {
    String fileName = uploadImageReqDTO.getFileName();
    String fileExtension = getFileExtension(fileName);
    String uniqueFileName = generateUniqueFileName(fileName);

    if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
      throw new InvalidFileTypeException(ErrorMessage.INVALID_FILE_TYPE);
    }

    UploadImageResDTO response = new UploadImageResDTO();
    response.setSignedUrl(generateSignedUrl(uniqueFileName));
    return response;
  }

  @Override
  public UploadImageResDTO createSignedUrlWithDirectory(UploadImageReqDTO uploadImageReqDTO,
      String directory) {
    String fileName = uploadImageReqDTO.getFileName();
    String fileExtension = getFileExtension(fileName);
    String uniqueFileName = generateUniqueFileName(fileName);

    if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
      throw new InvalidFileTypeException(ErrorMessage.INVALID_FILE_TYPE);
    }

    // Append directory to file name
    String fileNameWithDirectory =
        directory.endsWith("/") ? directory + uniqueFileName : directory + "/" + uniqueFileName;

    UploadImageResDTO response = new UploadImageResDTO();
    response.setSignedUrl(generateSignedUrl(fileNameWithDirectory));
    return response;
  }

  @Override
  public MultiMediaUploadResDTO createMultiMediaSignedUrlsWithDirectory(
      MultiMediaUploadReqDTO uploadRequestDTO, String directory) {
    MultiMediaUploadResDTO response = new MultiMediaUploadResDTO();
    List<MultiMediaUploadResDTO.SignedUrlDTO> signedUrls = new ArrayList<>();

    for (String fileName : uploadRequestDTO.getFileNames()) {
      String fileExtension = getFileExtension(fileName);

      if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
        throw new InvalidFileTypeException(ErrorMessage.INVALID_FILE_TYPE);
      }

      // Generate unique filename while preserving original name
      String uniqueFileName = generateUniqueFileName(fileName);
      String fileNameWithDirectory =
          directory.endsWith("/") ? directory + uniqueFileName : directory + "/" + uniqueFileName;

      String signedUrl = generateSignedUrl(fileNameWithDirectory);

      MultiMediaUploadResDTO.SignedUrlDTO urlDTO = new MultiMediaUploadResDTO.SignedUrlDTO();
      urlDTO.setFileName(fileName); // Return the unique filename so client knows what to
                                          // reference
      urlDTO.setSignedUrl(signedUrl);
      signedUrls.add(urlDTO);
    }

    response.setSignedUrls(signedUrls);
    return response;
  }

  private String generateUniqueFileName(String originalFileName) {
    String extension = getFileExtension(originalFileName);
    String nameWithoutExtension = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
    return UUID.randomUUID().toString() + "_" + nameWithoutExtension + "." + extension;
  }

  private String getFileExtension(String fileName) {
    if (fileName == null || fileName.lastIndexOf(".") == -1) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
  }

  private String generateSignedUrl(String fileName) {
    try {
      BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, fileName))
          .setContentType(determineContentType(fileName)).build();

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

  private String determineContentType(String fileName) {
    String extension = getFileExtension(fileName);
    switch (extension) {
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "png":
        return "image/png";
      case "gif":
        return "image/gif";
      case "webp":
        return "image/webp";
      case "mp4":
        return "video/mp4";
      case "mov":
        return "video/quicktime";
      default:
        return "application/octet-stream";
    }
  }
}

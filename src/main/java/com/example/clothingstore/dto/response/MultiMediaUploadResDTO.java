package com.example.clothingstore.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class MultiMediaUploadResDTO {
    private List<SignedUrlDTO> signedUrls;
    
    @Data
    public static class SignedUrlDTO {
        private String fileName;
        private String signedUrl;
    }
} 
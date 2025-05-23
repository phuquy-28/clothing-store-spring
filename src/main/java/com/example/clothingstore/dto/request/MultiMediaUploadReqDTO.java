package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class MultiMediaUploadReqDTO {
    @NotEmpty(message = "fileNames.not.empty")
    private List<String> fileNames;
} 
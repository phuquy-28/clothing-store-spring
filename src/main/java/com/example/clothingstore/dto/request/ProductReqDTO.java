package com.example.clothingstore.dto.request;

import java.util.List;
import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductReqDTO {

  private Long id;

  @NotBlank(message = "product.name.not.blank")
  private String name;

  @NotBlank(message = "product.description.not.blank")
  private String description;

  @NotNull(message = "product.price.not.null")
  @Min(value = 0, message = "product.price.min")
  private Double price;

  @NotNull(message = "product.category.not.null")
  private Long categoryId;

  @NotEmpty(message = "product.images.not.empty")
  @Valid
  private List<ProductImageReqDTO> images;

  @Valid
  private List<ProductVariantReqDTO> variants;

  @Data
  public static class ProductImageReqDTO {

    private Long id;

    private String url;
  }

  @Data
  public static class ProductVariantReqDTO {

    private Long id;

    @EnumValue(enumClass = Color.class, message = "color.invalid")
    private String color;

    @EnumValue(enumClass = Size.class, message = "size.invalid")
    private String size;

    private Integer quantity;
  }
}

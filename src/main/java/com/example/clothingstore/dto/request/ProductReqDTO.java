package com.example.clothingstore.dto.request;

import java.util.List;
import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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

  private Boolean isFeatured = false;

  @EnumValue(enumClass = Color.class, message = "color.invalid")
  private String colorDefault;

  @NotEmpty(message = "product.images.not.empty")
  private List<String> images;

  @Valid
  private List<ProductVariantReqDTO> variants;

  @Data
  public static class ProductVariantReqDTO {

    private Long id;

    @EnumValue(enumClass = Color.class, message = "color.invalid")
    private String color;

    @EnumValue(enumClass = Size.class, message = "size.invalid")
    private String size;

    @Min(value = 0, message = "variant.quantity.min")
    private Integer quantity;

    @NotNull(message = "variant.differencePrice.not.null")
    private Double differencePrice;

    @NotEmpty(message = "variant.images.not.empty")
    private List<String> images;
  }
}

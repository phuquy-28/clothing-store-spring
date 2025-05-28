package com.example.clothingstore.enumeration;

import com.example.clothingstore.exception.BadRequestException;
import lombok.Getter;

@Getter
public enum TemplateType {
  PRODUCT("products", "product_import_template.xlsx", "File mẫu import sản phẩm"),
  CATEGORY("categories", "category_import_template.xlsx", "File mẫu import danh mục");

  private final String typeName;
  private final String fileName;
  private final String description;

  TemplateType(String typeName, String fileName, String description) {
    this.typeName = typeName;
    this.fileName = fileName;
    this.description = description;
  }

  public static TemplateType fromTypeName(String typeName) {
    for (TemplateType type : TemplateType.values()) {
      if (type.typeName.equalsIgnoreCase(typeName)) {
        return type;
      }
    }
    throw new BadRequestException("Không tìm thấy loại template: " + typeName);
  }
}

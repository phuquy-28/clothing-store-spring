package com.example.clothingstore.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.example.clothingstore.config.Translator;
import com.example.clothingstore.dto.response.ProductImportResponseDTO;
import com.example.clothingstore.entity.Category;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductImage;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.ImportMode;
import com.example.clothingstore.enumeration.Size;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.DataValidationException;
import com.example.clothingstore.repository.CategoryRepository;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.service.ImportService;
import com.example.clothingstore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RequiredArgsConstructor
@Service
public class ImportServiceImpl implements ImportService {

  private final Logger log = LoggerFactory.getLogger(ImportServiceImpl.class);
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductService productService;

  @Value("${spring.servlet.multipart.max-file-size}")
  private String maxFileSizeConfig;

  private static final List<String> ALLOWED_EXCEL_CONTENT_TYPES =
      Arrays.asList("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
  private static final String ALLOWED_EXCEL_EXTENSION = "xlsx";


  // Định nghĩa các chỉ số cột (giữ nguyên)
  private static final int COL_PRODUCT_NAME = 0;
  private static final int COL_DESCRIPTION = 1;
  private static final int COL_PRICE = 2;
  private static final int COL_CATEGORY_NAME = 3;
  private static final int COL_IS_FEATURED = 4;
  private static final int COL_PRODUCT_IMAGE_URL_1 = 5;
  // private static final int COL_PRODUCT_IMAGE_URL_2 = 6;
  // private static final int COL_PRODUCT_IMAGE_URL_3 = 7;
  // private static final int COL_PRODUCT_IMAGE_URL_4 = 8;
  private static final int COL_VARIANT_COLOR = 9;
  private static final int COL_VARIANT_SIZE = 10;
  private static final int COL_VARIANT_QUANTITY = 11;
  private static final int COL_VARIANT_DIFFERENCE_PRICE = 12;
  private static final int COL_VARIANT_IMAGE_URL_1 = 13;
  // private static final int COL_VARIANT_IMAGE_URL_2 = 14;

  @Override
  public Resource getImportTemplateFile(
      com.example.clothingstore.enumeration.TemplateType templateType) {
    String basePath = "templates/excel_templates/";
    return new ClassPathResource(basePath + templateType.getFileName());
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ProductImportResponseDTO importProducts(MultipartFile file, ImportMode importMode) {
    // --- Kiểm tra bảo mật cơ bản ---
    if (file.isEmpty()) {
      throw new BadRequestException(Translator.toLocale("file.not.blank"));
    }

    // Kiểm tra Content Type
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_EXCEL_CONTENT_TYPES.contains(contentType.toLowerCase())) {
      log.warn("Loại nội dung file không hợp lệ: {}. Tên file: {}", contentType,
          file.getOriginalFilename());
      throw new BadRequestException(Translator.toLocale("import.error.invalid_content_type"));
    }

    // Kiểm tra phần mở rộng file
    String originalFilename = file.getOriginalFilename();
    String extension = FilenameUtils.getExtension(originalFilename); // Sử dụng Apache Commons IO
    if (extension == null || !ALLOWED_EXCEL_EXTENSION.equalsIgnoreCase(extension)) {
      log.warn("Phần mở rộng file không hợp lệ: {}. Tên file: {}", extension, originalFilename);
      throw new BadRequestException(
          Translator.toLocale("import.error.invalid_extension", ALLOWED_EXCEL_EXTENSION));
    }
    // --- Kết thúc kiểm tra bảo mật cơ bản ---


    if (importMode != ImportMode.ADD_ONLY) {
      throw new UnsupportedOperationException(
          Translator.toLocale("import.error.unsupported_mode", importMode.toString()));
    }

    List<Product> productsToSave = new ArrayList<>();
    int totalRowsRead = 0;
    int successfulEntityGroups = 0;

    try (InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream)) {

      Sheet sheet = workbook.getSheet("Dữ liệu sản phẩm");
      if (sheet == null) {
        throw new DataValidationException("import.sheet.not_found", "Dữ liệu sản phẩm");
      }

      Iterator<Row> rowIterator = sheet.iterator();

      if (rowIterator.hasNext()) {
        rowIterator.next(); // Bỏ qua dòng tiêu đề
        totalRowsRead++;
      }

      Product currentProduct = null;
      int currentRowNum = 1;

      while (rowIterator.hasNext()) {
        Row currentRow = rowIterator.next();
        currentRowNum++;
        totalRowsRead++;

        if (isRowEmpty(currentRow)) {
          log.debug("Bỏ qua dòng trống số {}", currentRowNum);
          totalRowsRead--;
          continue;
        }

        String productName = getStringCellValue(currentRow, COL_PRODUCT_NAME);

        try {
          if (StringUtils.hasText(productName)) {
            if (currentProduct != null && !currentProduct.getVariants().isEmpty()) {
              productsToSave.add(currentProduct);
            }
            currentProduct = new Product();
            currentProduct.setVariants(new ArrayList<>());
            currentProduct.setImages(new ArrayList<>());

            currentProduct.setName(productName.trim());
            validateAndSetProductData(currentRow, currentProduct, currentRowNum);

            ProductVariant firstVariant = new ProductVariant();
            validateAndSetVariantData(currentRow, firstVariant, currentProduct, currentRowNum);
            currentProduct.getVariants().add(firstVariant);

          } else if (currentProduct != null) {
            String colorStr = getStringCellValue(currentRow, COL_VARIANT_COLOR);
            String sizeStr = getStringCellValue(currentRow, COL_VARIANT_SIZE);
            if (!StringUtils.hasText(colorStr) && !StringUtils.hasText(sizeStr)) {
              log.warn(
                  "Dòng {} không có tên sản phẩm và cũng không có thông tin màu/size cho biến thể. Bỏ qua.",
                  currentRowNum);
              continue;
            }
            ProductVariant additionalVariant = new ProductVariant();
            validateAndSetVariantData(currentRow, additionalVariant, currentProduct, currentRowNum);
            currentProduct.getVariants().add(additionalVariant);
          } else {
            throw new DataValidationException("import.error.row.missing_product_name",
                String.valueOf(currentRowNum));
          }
        } catch (DataValidationException e) {
          String errorMessage = Translator.toLocale(e.getMessageKey(),
              e.getArgs() != null ? e.getArgs() : new Object[] {});
          log.warn("Lỗi xác thực dữ liệu ở dòng {}: {}", currentRowNum, errorMessage);
          throw e; // Ném lại để transaction rollback
        } catch (Exception e) {
          log.error("Lỗi không xác định khi xử lý dòng {}: {}", currentRowNum, e.getMessage(), e);
          throw new RuntimeException(Translator.toLocale("import.error.row.unknown",
              String.valueOf(currentRowNum), e.getMessage()), e);
        }
      }

      if (currentProduct != null && !currentProduct.getVariants().isEmpty()) {
        productsToSave.add(currentProduct);
      }

      if (productsToSave.isEmpty()) {
        if (totalRowsRead > 1) {
          throw new DataValidationException("import.error.no_valid_products");
        } else {
          throw new DataValidationException("import.error.empty_file_no_data");
        }
      }

      productRepository.saveAll(productsToSave);
      successfulEntityGroups = productsToSave.size();
      log.info("Đã lưu thành công {} nhóm sản phẩm.", successfulEntityGroups);

    } catch (IOException e) {
      log.error("Lỗi khi đọc file Excel '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
      throw new RuntimeException(Translator.toLocale("excel.read.error"), e);
    }

    return ProductImportResponseDTO.builder().totalRowsRead(totalRowsRead - 1)
        .successfulImports(successfulEntityGroups).failedImports(0).errorMessages(new ArrayList<>())
        .successMessage(Translator.toLocale("import.success.total", successfulEntityGroups))
        .build();
  }

  private void validateAndSetProductData(Row currentRow, Product product, int rowNum)
      throws DataValidationException {
    String productName = product.getName(); // Đã được set từ trước
    if (productRepository.findByName(productName).isPresent()) {
      throw new DataValidationException("import.error.row.col.product_name_exists",
          String.valueOf(rowNum), productName);
    }

    String description = getStringCellValue(currentRow, COL_DESCRIPTION);
    if (!StringUtils.hasText(description)) {
      throw new DataValidationException("import.error.row.col.blank", String.valueOf(rowNum),
          "Mô tả sản phẩm");
    }
    product.setDescription(description);

    Double price = getNumericCellValue(currentRow, COL_PRICE);
    if (price == null || price <= 0) {
      throw new DataValidationException("import.error.row.col.invalid_price",
          String.valueOf(rowNum), "Giá sản phẩm");
    }
    product.setPrice(price);

    String categoryName = getStringCellValue(currentRow, COL_CATEGORY_NAME);
    if (!StringUtils.hasText(categoryName)) {
      throw new DataValidationException("import.error.row.col.blank", String.valueOf(rowNum),
          "Tên danh mục");
    }
    Category category = categoryRepository.findByName(categoryName.trim())
        .orElseThrow(() -> new DataValidationException("import.error.row.col.category_not_found",
            String.valueOf(rowNum), categoryName));
    product.setCategory(category);

    Boolean isFeatured = getBooleanCellValue(currentRow, COL_IS_FEATURED);
    product.setFeatured(isFeatured != null ? isFeatured : false);

    product.setSlug(productService.createSlug(product.getName())); //

    boolean firstImageFound = false;
    for (int i = 0; i < 4; i++) {
      String imageUrl = getStringCellValue(currentRow, COL_PRODUCT_IMAGE_URL_1 + i);
      if (StringUtils.hasText(imageUrl)) {
        ProductImage productImage = new ProductImage();
        productImage.setPublicUrl(imageUrl.trim());
        productImage.setImageOrder(i);
        productImage.setProduct(product);
        product.getImages().add(productImage);
        if (i == 0)
          firstImageFound = true;
      } else if (i == 0 && !firstImageFound) {
        // Sửa: Chỉ ném lỗi nếu *tất cả* các cột ảnh đều trống VÀ cột ảnh đầu tiên trống
      }
    }
    if (!firstImageFound && product.getImages().isEmpty()) {
      throw new DataValidationException("import.error.row.col.blank", String.valueOf(rowNum),
          "URL Ảnh SP 1");
    }
  }

  private void validateAndSetVariantData(Row currentRow, ProductVariant variant, Product product,
      int rowNum) throws DataValidationException {
    variant.setProduct(product);

    String colorStr = getStringCellValue(currentRow, COL_VARIANT_COLOR);
    if (!StringUtils.hasText(colorStr)) {
      throw new DataValidationException("import.error.row.col.blank", String.valueOf(rowNum),
          "Màu Biến thể");
    }
    try {
      variant.setColor(Color.valueOf(colorStr.trim().toUpperCase()));
    } catch (IllegalArgumentException e) {
      throw new DataValidationException("import.error.row.col.invalid_color",
          String.valueOf(rowNum), colorStr);
    }

    String sizeStr = getStringCellValue(currentRow, COL_VARIANT_SIZE);
    if (!StringUtils.hasText(sizeStr)) {
      throw new DataValidationException("import.error.row.col.blank", String.valueOf(rowNum),
          "Kích thước Biến thể");
    }
    try {
      variant.setSize(Size.valueOf(sizeStr.trim().toUpperCase()));
    } catch (IllegalArgumentException e) {
      throw new DataValidationException("import.error.row.col.invalid_size", String.valueOf(rowNum),
          sizeStr);
    }

    for (ProductVariant existingVariant : product.getVariants()) {
      if (existingVariant.getColor() == variant.getColor()
          && existingVariant.getSize() == variant.getSize()) {
        throw new DataValidationException("import.error.row.col.duplicate_variant",
            String.valueOf(rowNum), colorStr, sizeStr);
      }
    }

    Double quantityDouble = getNumericCellValue(currentRow, COL_VARIANT_QUANTITY);
    if (quantityDouble == null || quantityDouble < 0
        || quantityDouble.intValue() != quantityDouble) {
      throw new DataValidationException("import.error.row.col.invalid_quantity",
          String.valueOf(rowNum), "Số lượng Biến thể");
    }
    variant.setQuantity(quantityDouble.intValue());

    Double diffPrice = getNumericCellValue(currentRow, COL_VARIANT_DIFFERENCE_PRICE);
    if (diffPrice == null) {
      throw new DataValidationException("import.error.row.col.blank", String.valueOf(rowNum),
          "Chênh lệch giá Biến thể");
    }
    variant.setDifferencePrice(diffPrice);

    variant.setImages(new ArrayList<>());
    boolean firstVariantImageFound = false;
    for (int i = 0; i < 2; i++) { // Giả sử tối đa 2 ảnh biến thể
      String imageUrl = getStringCellValue(currentRow, COL_VARIANT_IMAGE_URL_1 + i);
      if (StringUtils.hasText(imageUrl)) {
        ProductImage variantImage = new ProductImage();
        variantImage.setPublicUrl(imageUrl.trim());
        variantImage.setImageOrder(i);
        variantImage.setProductVariant(variant);
        variant.getImages().add(variantImage);
        if (i == 0)
          firstVariantImageFound = true;
      } else if (i == 0 && !firstVariantImageFound) {
        // Tương tự ảnh sản phẩm chính
      }
    }
    if (!firstVariantImageFound && variant.getImages().isEmpty()) {
      throw new DataValidationException("import.error.row.col.blank", String.valueOf(rowNum),
          "URL Ảnh Biến thể 1");
    }
  }

  private String getStringCellValue(Row row, int cellNum) {
    Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) {
      return null;
    }
    if (cell.getCellType() == CellType.STRING) {
      return cell.getStringCellValue().trim();
    } else if (cell.getCellType() == CellType.NUMERIC) {
      DataFormatter formatter = new DataFormatter();
      return formatter.formatCellValue(cell).trim();
    } else if (cell.getCellType() == CellType.BOOLEAN) {
      return String.valueOf(cell.getBooleanCellValue()).toUpperCase();
    }
    return null;
  }

  private Double getNumericCellValue(Row row, int cellNum) {
    Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      return cell.getNumericCellValue();
    } else if (cell.getCellType() == CellType.STRING) {
      try {
        String cellValue = cell.getStringCellValue().trim();
        if (cellValue.isEmpty())
          return null; // Coi chuỗi rỗng là null cho số
        return Double.parseDouble(cellValue);
      } catch (NumberFormatException e) {
        // Ném lỗi cụ thể nếu không thể parse, kèm theo giá trị cell và vị trí
        throw new DataValidationException("import.error.row.col.invalid_number_format",
            String.valueOf(row.getRowNum() + 1), getStringCellValue(row, cellNum));
      }
    }
    // Nếu không phải NUMERIC hoặc STRING (mà có thể parse thành số), thì là lỗi
    throw new DataValidationException("import.error.row.col.expected_numeric",
        String.valueOf(row.getRowNum() + 1), getCellAddress(cell));
  }

  private Boolean getBooleanCellValue(Row row, int cellNum) {
    Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null; // Cho phép để trống, sẽ được coi là false trong logic set isFeatured
    }
    if (cell.getCellType() == CellType.BOOLEAN) {
      return cell.getBooleanCellValue();
    } else if (cell.getCellType() == CellType.STRING) {
      String val = cell.getStringCellValue().trim().toUpperCase();
      if ("TRUE".equals(val))
        return true;
      if ("FALSE".equals(val))
        return false;
      // Nếu không phải TRUE/FALSE, ném lỗi
      throw new DataValidationException("import.error.row.col.invalid_boolean",
          String.valueOf(row.getRowNum() + 1), val);
    }
    // Nếu không phải BOOLEAN hoặc STRING (mà có thể parse thành boolean), thì là lỗi
    throw new DataValidationException("import.error.row.col.expected_boolean",
        String.valueOf(row.getRowNum() + 1), getCellAddress(cell));
  }

  private boolean isRowEmpty(Row row) {
    if (row == null) {
      return true;
    }
    if (row.getLastCellNum() <= 0) {
      return true;
    }
    for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
      Cell cell = row.getCell(cellNum);
      if (cell != null && cell.getCellType() != CellType.BLANK
          && StringUtils.hasText(cell.toString())) {
        return false;
      }
    }
    return true;
  }

  private String getCellAddress(Cell cell) {
    if (cell == null)
      return "";
    return new CellReference(cell).formatAsString();
  }
}

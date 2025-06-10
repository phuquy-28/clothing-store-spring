package com.example.clothingstore.service.impl;

import com.example.clothingstore.annotation.EnableSoftDeleteFilter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.ProductReqDTO;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO.Meta;
import com.example.clothingstore.dto.response.ReviewProductDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.entity.Cart;
import com.example.clothingstore.entity.CartItem;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductImage;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.Review;
import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.enumeration.Size;
import com.example.clothingstore.exception.ResourceAlreadyExistException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.CartRepository;
import com.example.clothingstore.repository.CategoryRepository;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.repository.ReviewRepository;
import com.example.clothingstore.service.CloudStorageService;
import com.example.clothingstore.service.ProductService;
import com.example.clothingstore.service.PromotionCalculatorService;
import com.example.clothingstore.specification.ProductSpecification;
import com.example.clothingstore.specification.ReviewSpecification;
import com.example.clothingstore.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final CloudStorageService cloudStorageService;

  private final ProductRepository productRepository;

  private final CategoryRepository categoryRepository;

  private final PromotionCalculatorService promotionCalculatorService;

  private final ReviewRepository reviewRepository;

  private final CartRepository cartRepository;

  @Override
  public UploadImageResDTO createSignedUrl(UploadImageReqDTO uploadImageReqDTO) {
    return cloudStorageService.createSignedUrlWithDirectory(uploadImageReqDTO, "products-images");
  }

  @Override
  public ProductResDTO createProduct(ProductReqDTO productReqDTO) {
    List<Product> existingProducts = productRepository.findByNameAndIsDeletedFalse(productReqDTO.getName());
    if (!existingProducts.isEmpty()) {
      throw new ResourceAlreadyExistException(ErrorMessage.PRODUCT_ALREADY_EXISTS);
    }

    Product product = new Product();
    product.setName(productReqDTO.getName());
    product.setDescription(productReqDTO.getDescription());
    product.setPrice(productReqDTO.getPrice());
    product.setCategory(categoryRepository.findById(productReqDTO.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND)));
    product.setFeatured(productReqDTO.getFeatured());

    final Product finalProduct = product;
    product.setImages(IntStream.range(0, productReqDTO.getImages().size()).mapToObj(index -> {
        ProductImage productImage = new ProductImage();
        productImage.setPublicUrl(productReqDTO.getImages().get(index));
        productImage.setImageOrder(index);
        productImage.setProduct(finalProduct);
        return productImage;
    }).collect(Collectors.toList()));

    product.setVariants(productReqDTO.getVariants().stream().map(variant -> {
        ProductVariant productVariant = new ProductVariant();
        productVariant.setColor(Color.valueOf(variant.getColor().toUpperCase()));
        productVariant.setSize(Size.valueOf(variant.getSize().toUpperCase()));
        productVariant.setQuantity(variant.getQuantity());
        productVariant.setDifferencePrice(variant.getDifferencePrice());
        productVariant.setProduct(finalProduct);

        // Handle variant images
        List<ProductImage> variantImages = IntStream.range(0, variant.getImages().size())
            .mapToObj(index -> {
                ProductImage productImage = new ProductImage();
                productImage.setPublicUrl(variant.getImages().get(index));
                productImage.setImageOrder(0);
                productImage.setProductVariant(productVariant);
                return productImage;
            }).collect(Collectors.toList());
        productVariant.setImages(variantImages);

        return productVariant;
    }).collect(Collectors.toList())); 


    // product = productRepository.save(product);
    
    String slug = createSlug(product.getName()) + "-" + System.currentTimeMillis();
    product.setSlug(slug);

    log.debug("Product created with id: {}", product.getId());
    log.debug("Slug created: {}", slug);

    // Save product to generate IDs for product and variants
    product = productRepository.save(product);

    // Generate SKUs for each variant
    for (ProductVariant variant : product.getVariants()) {
      variant.setSku(generateMeaningfulSku(variant));
    }

    product = productRepository.save(product);

    return convertToProductResDTO(product);
  }

  @Override
  public String generateMeaningfulSku(ProductVariant variant) {
    if (variant.getProduct() == null || variant.getProduct().getId() == null) {
        throw new IllegalStateException("Product and Product ID must not be null to generate SKU.");
    }
    String productId = variant.getProduct().getId().toString();
    String colorCode = variant.getColor().name().substring(0, Math.min(3, variant.getColor().name().length()));
    String sizeCode = variant.getSize().name();
    
    return String.format("%s-%s-%s", productId, colorCode, sizeCode).toUpperCase();
}

  @Override
  @EnableSoftDeleteFilter
  public ProductResDTO getProductBySlug(String slug) {
    Product product = productRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PRODUCT_NOT_FOUND));
    return convertToProductResDTO(product);
  }

  // @Override
  // public ResultPaginationDTO getProducts(Specification<Product> specification, Pageable pageable) {
  //   // Kết hợp specification từ spring-filter với custom specification
  //   Specification<Product> finalSpec = specification;

  //   // Lấy các parameters từ request context
  //   var request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
  //   String averageRatingStr = request.getParameter("averageRating");
  //   String hasDiscountStr = request.getParameter("hasDiscount");
  //   String minPriceStr = request.getParameter("minPrice");
  //   String maxPriceStr = request.getParameter("maxPrice");
  //   String sizesStr = request.getParameter("sizes");

  //   // Xử lý specification cho sizes
  //   if (sizesStr != null && !sizesStr.isEmpty()) {
  //     Set<Size> requestedSizes = Arrays.stream(sizesStr.split(","))
  //         .map(s -> Size.valueOf(s.trim().toUpperCase())).collect(Collectors.toSet());

  //     Specification<Product> sizesSpec = (root, query, cb) -> {
  //       query.distinct(true);

  //       // Tạo subquery để kiểm tra từng size
  //       List<Predicate> sizePredicates = requestedSizes.stream().map(size -> {
  //         Subquery<ProductVariant> variantSubquery = query.subquery(ProductVariant.class);
  //         Root<ProductVariant> variantRoot = variantSubquery.from(ProductVariant.class);

  //         // Kiểm tra tồn tại ít nhất 1 variant cho mỗi size
  //         variantSubquery.select(variantRoot)
  //             .where(cb.and(cb.equal(variantRoot.get("product"), root),
  //                 cb.equal(variantRoot.get("size"), size)));

  //         return cb.exists(variantSubquery);
  //       }).collect(Collectors.toList());

  //       // Kết hợp tất cả các điều kiện với AND
  //       return cb.and(sizePredicates.toArray(new Predicate[0]));
  //     };

  //     finalSpec = finalSpec != null ? finalSpec.and(sizesSpec) : sizesSpec;
  //   }

  //   // Xử lý specification cho averageRating
  //   if (averageRatingStr != null) {
  //       double rating = Double.parseDouble(averageRatingStr);
  //       Specification<Product> ratingSpec = (root, query, cb) -> {
  //           query.distinct(true);
  //           Subquery<Double> avgRatingSubquery = query.subquery(Double.class);
  //           Root<Review> reviewRoot = avgRatingSubquery.from(Review.class);
  //           avgRatingSubquery.select(cb.avg(reviewRoot.get("rating")))
  //               .where(cb.equal(reviewRoot.get("product"), root));
  //           return cb.greaterThanOrEqualTo(avgRatingSubquery, rating);
  //       };
  //       finalSpec = finalSpec != null ? finalSpec.and(ratingSpec) : ratingSpec;
  //   }

  //   // Xử lý specification cho hasDiscount
  //   if ("true".equalsIgnoreCase(hasDiscountStr)) {
  //       Instant now = Instant.now();
  //       Specification<Product> discountSpec = (root, query, cb) -> {
  //           query.distinct(true);
  //           Join<Product, Promotion> productPromotions = root.join("promotions", JoinType.LEFT);
  //           Join<Product, Category> category = root.join("category", JoinType.LEFT);
  //           Join<Category, Promotion> categoryPromotions = category.join("promotions", JoinType.LEFT);

  //           return cb.or(
  //               cb.and(cb.lessThan(productPromotions.get("startDate"), now),
  //                   cb.greaterThan(productPromotions.get("endDate"), now)),
  //               cb.and(cb.lessThan(categoryPromotions.get("startDate"), now),
  //                   cb.greaterThan(categoryPromotions.get("endDate"), now)));
  //       };
  //       finalSpec = finalSpec != null ? finalSpec.and(discountSpec) : discountSpec;
  //   }

  //   // Lấy tất cả products theo specification
  //   List<Product> allProducts = productRepository.findAll(finalSpec);

  //   // Lọc theo price range nếu có
  //   if (minPriceStr != null || maxPriceStr != null) {
  //       Double minPrice = minPriceStr != null ? Double.parseDouble(minPriceStr) : null;
  //       Double maxPrice = maxPriceStr != null ? Double.parseDouble(maxPriceStr) : null;

  //       allProducts = allProducts.stream()
  //           .filter(product -> {
  //               Double productMinPriceWithDiscount = promotionCalculatorService.calculateMinPriceWithDiscount(product);
  //               boolean meetsMinPrice = minPrice == null || productMinPriceWithDiscount >= minPrice;
  //               boolean meetsMaxPrice = maxPrice == null || productMinPriceWithDiscount <= maxPrice;
  //               return meetsMinPrice && meetsMaxPrice;
  //           })
  //           .collect(Collectors.toList());
  //   }

  //   // Sắp xếp sản phẩm
  //   Comparator<Product> comparator = null;
  //   if (pageable.getSort().isSorted()) {
  //       for (Sort.Order order : pageable.getSort()) {
  //           Comparator<Product> currentComparator = switch (order.getProperty()) {
  //               case "createdAt" -> Comparator.comparing(Product::getCreatedAt);
  //               case "minPriceWithDiscount" -> Comparator.comparing(
  //                   product -> promotionCalculatorService.calculateMinPriceWithDiscount(product));
  //               default -> Comparator.comparing(Product::getCreatedAt);
  //           };

  //           if (order.getDirection() == Sort.Direction.DESC) {
  //               currentComparator = currentComparator.reversed();
  //           }

  //           comparator = comparator == null ? currentComparator : comparator.thenComparing(currentComparator);
  //       }
        
  //       if (comparator != null) {
  //           allProducts.sort(comparator);
  //       }
  //   }

  //   // Kiểm tra và xử lý phân trang
  //   int start = (int) pageable.getOffset();
    
  //   // Nếu start index vượt quá size của list, trả về trang trống
  //   if (start >= allProducts.size()) {
  //       return ResultPaginationDTO.builder()
  //           .meta(Meta.builder()
  //               .page((long) pageable.getPageNumber())
  //               .pageSize((long) pageable.getPageSize())
  //               .total((long) allProducts.size())
  //               .pages((long) Math.ceil((double) allProducts.size() / pageable.getPageSize()))
  //               .build())
  //           .data(new ArrayList<>())
  //           .build();
  //   }

  //   // Tính toán end index, đảm bảo không vượt quá size của list
  //   int end = Math.min((start + pageable.getPageSize()), allProducts.size());
  //   List<Product> paginatedProducts = allProducts.subList(start, end);

  //   // Tạo Page object
  //   Page<Product> productPage = new PageImpl<>(paginatedProducts, pageable, allProducts.size());

  //   // Trả về kết quả
  //   return ResultPaginationDTO.builder()
  //       .meta(Meta.builder()
  //           .page((long) productPage.getNumber())
  //           .pageSize((long) productPage.getSize())
  //           .pages((long) productPage.getTotalPages())
  //           .total(productPage.getTotalElements())
  //           .build())
  //       .data(productPage.getContent().stream()
  //           .map(this::convertToProductResDTO)
  //           .collect(Collectors.toList()))
  //       .build();
  // }

  @Override
 @EnableSoftDeleteFilter
  public ResultPaginationDTO getProducts(Boolean isBestSeller, Boolean isDiscounted, Integer days,
      Double averageRating, Boolean hasDiscount, Double minPrice, Double maxPrice, String sizes,
      String sortField, String sortOrder, Specification<Product> specification, Pageable pageable) {

    log.debug(
        "isBestSeller: {}, isDiscounted: {}, days: {}, averageRating: {}, hasDiscount: {}, minPrice: {}, maxPrice: {}, sizes: {}, sortField: {}, sortOrder: {}",
        isBestSeller, isDiscounted, days, averageRating, hasDiscount, minPrice, maxPrice, sizes,
        sortField, sortOrder);

    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    boolean hasBestSellerParam = request.getParameterMap().containsKey("isBestSeller");
    boolean hasDiscountedParam = request.getParameterMap().containsKey("isDiscounted");

    Specification<Product> finalSpec = specification;

    if (isBestSeller || hasBestSellerParam) {
      if (days != null) {
        Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
        finalSpec =
            finalSpec.and(ProductSpecification.isBestSeller(PaymentStatus.SUCCESS, startDate));
      } else {
        finalSpec =
            finalSpec.and(ProductSpecification.isBestSeller(PaymentStatus.SUCCESS, null));
      }
    }

    if (isDiscounted || hasDiscountedParam) {
      finalSpec = finalSpec.and(ProductSpecification.isDiscountedWithMaxDiscount(Instant.now()));
    }

    if (sortField != null) {
      finalSpec = finalSpec.and(ProductSpecification.sortBy(sortField, sortOrder));
    }

    if (averageRating != null) {
      finalSpec = finalSpec.and(ProductSpecification.averageRatingGreaterThanOrEqualTo(averageRating));
    }

    if (minPrice != null ) {
      finalSpec = finalSpec.and(ProductSpecification.minPriceGreaterThanOrEqualTo(minPrice));
    }

    if (maxPrice != null) {
      finalSpec = finalSpec.and(ProductSpecification.maxPriceLessThanOrEqualTo(maxPrice));
    }

    Page<Product> allProducts = productRepository.findAll(finalSpec, pageable);

    return ResultPaginationDTO.builder()
        .meta(Meta.builder().page((long) allProducts.getNumber())
            .pageSize((long) allProducts.getSize()).pages((long) allProducts.getTotalPages())
            .total(allProducts.getTotalElements()).build())
        .data(allProducts.stream().map(this::convertToProductResDTO).collect(Collectors.toList()))
        .build();
  }

  @Override
  @Transactional
  public ProductResDTO updateProduct(ProductReqDTO productReqDTO) {
    Product product = productRepository.findById(productReqDTO.getId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PRODUCT_NOT_FOUND));
    
    product.setName(productReqDTO.getName());
    product.setDescription(productReqDTO.getDescription());
    product.setPrice(productReqDTO.getPrice());
    product.setCategory(categoryRepository.findById(productReqDTO.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND)));
    product.setFeatured(productReqDTO.getFeatured());
    
    // Update slug
    String slug = createSlug(product.getName()) + "-" + System.currentTimeMillis();
    product.setSlug(slug);

    // Update product images
    updateProductImages(product, productReqDTO.getImages());

    // Update product variants
    updateProductVariants(product, productReqDTO.getVariants());

    product = productRepository.save(product);
    return convertToProductResDTO(product);
  }

  private void updateProductImages(Product product, List<String> newImageUrls) {
    List<ProductImage> currentImages = product.getImages();
    List<ProductImage> updatedImages = new ArrayList<>();
    
    for (int i = 0; i < newImageUrls.size(); i++) {
        String imageUrl = newImageUrls.get(i);
        ProductImage productImage = currentImages.stream()
            .filter(img -> img.getPublicUrl().equals(imageUrl))
            .findFirst()
            .orElseGet(ProductImage::new);

        productImage.setPublicUrl(imageUrl);
        productImage.setImageOrder(i);
        productImage.setProduct(product);
        updatedImages.add(productImage);
    }
    
    product.getImages().clear();
    product.getImages().addAll(updatedImages);
  }

  private void updateProductVariants(Product product, List<ProductReqDTO.ProductVariantReqDTO> variantReqDTOs) {
    List<ProductVariant> currentVariants = new ArrayList<>(product.getVariants());
    List<ProductVariant> updatedVariants = new ArrayList<>();
    
    for (ProductReqDTO.ProductVariantReqDTO variantDTO : variantReqDTOs) {
        ProductVariant productVariant;
        if (variantDTO.getId() != null) {
            // Existing variant
            productVariant = currentVariants.stream()
                .filter(v -> v.getId().equals(variantDTO.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantDTO.getId()));
            
            // Remove the found variant from currentVariants to avoid duplication
            currentVariants.removeIf(v -> v.getId().equals(variantDTO.getId()));
        } else {
            // New variant
            productVariant = new ProductVariant();
            productVariant.setProduct(product);
        }

        productVariant.setColor(Color.valueOf(variantDTO.getColor().toUpperCase()));
        productVariant.setSize(Size.valueOf(variantDTO.getSize().toUpperCase()));
        productVariant.setQuantity(variantDTO.getQuantity());
        productVariant.setDifferencePrice(variantDTO.getDifferencePrice());

        // Update variant images
        updateVariantImages(productVariant, variantDTO.getImages());

        updatedVariants.add(productVariant);
    }
    
    // Clear all existing variants and add the updated ones
    product.getVariants().clear();
    product.getVariants().addAll(updatedVariants);
  }

  private void updateVariantImages(ProductVariant variant, List<String> imageUrls) {
    if (variant.getImages() == null) {
        variant.setImages(new ArrayList<>());
    }
    List<ProductImage> variantImages = variant.getImages();
    List<ProductImage> updatedImages = new ArrayList<>();
    
    for (int i = 0; i < imageUrls.size(); i++) {
        String imageUrl = imageUrls.get(i);
        ProductImage productImage = variantImages.stream()
            .filter(img -> img.getPublicUrl().equals(imageUrl))
            .findFirst()
            .orElseGet(() -> {
                ProductImage newImage = new ProductImage();
                newImage.setProductVariant(variant);
                return newImage;
            });

        productImage.setPublicUrl(imageUrl);
        productImage.setImageOrder(i);
        updatedImages.add(productImage);
    }
    
    variantImages.clear();
    variantImages.addAll(updatedImages);
  }

  @Override
  public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PRODUCT_NOT_FOUND));
    productRepository.delete(product);
  }

  @Override
  @EnableSoftDeleteFilter
  public ProductResDTO getProductById(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PRODUCT_NOT_FOUND));
    return convertToProductResDTO(product);
  }

  @Override
  public String createSlug(String input) {
    // Replace Vietnamese characters with their non-accented equivalents
    String[] vietnameseChars = {
        "À", "Á", "Ạ", "Ả", "Ã", "Â", "Ầ", "Ấ", "Ậ", "Ẩ", "Ẫ", "Ă", "Ằ", "Ắ", "Ặ", "Ẳ", "Ẵ",
        "à", "á", "ạ", "ả", "ã", "â", "ầ", "ấ", "ậ", "ẩ", "ẫ", "ă", "ằ", "ắ", "ặ", "ẳ", "ẵ",
        "Đ", "đ",
        "È", "É", "Ẹ", "Ẻ", "Ẽ", "Ê", "Ề", "Ế", "Ệ", "Ể", "Ễ",
        "è", "é", "ẹ", "ẻ", "ẽ", "ê", "ề", "ế", "ệ", "ể", "ễ",
        "Ì", "Í", "Ị", "Ỉ", "Ĩ",
        "ì", "í", "ị", "ỉ", "ĩ",
        "Ò", "Ó", "Ọ", "Ỏ", "Õ", "Ô", "Ồ", "Ố", "Ộ", "Ổ", "Ỗ", "Ơ", "Ờ", "Ớ", "Ợ", "Ở", "Ỡ",
        "ò", "ó", "ọ", "ỏ", "õ", "ô", "ồ", "ố", "ộ", "ổ", "ỗ", "ơ", "ờ", "ớ", "ợ", "ở", "ỡ",
        "Ù", "Ú", "Ụ", "Ủ", "Ũ", "Ư", "Ừ", "Ứ", "Ự", "Ử", "Ữ",
        "ù", "ú", "ụ", "ủ", "ũ", "ư", "ừ", "ứ", "ự", "ử", "ữ",
        "Ỳ", "Ý", "Ỵ", "Ỷ", "Ỹ",
        "ỳ", "ý", "ỵ", "ỷ", "ỹ"
    };
    String[] nonAccentChars = {
        "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A",
        "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a",
        "D", "d",
        "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E",
        "e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "e",
        "I", "I", "I", "I", "I",
        "i", "i", "i", "i", "i",
        "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O",
        "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o",
        "U", "U", "U", "U", "U", "U", "U", "U", "U", "U", "U",
        "u", "u", "u", "u", "u", "u", "u", "u", "u", "u", "u",
        "Y", "Y", "Y", "Y", "Y",
        "y", "y", "y", "y", "y"
    };

    for (int i = 0; i < vietnameseChars.length; i++) {
      input = input.replace(vietnameseChars[i], nonAccentChars[i]);
    }

    input = input.toLowerCase();

    input = input.replaceAll("[^a-z0-9\\s-]", "");

    input = input.trim().replaceAll("\\s+", "-").replaceAll("-+", "-");

    return input.isEmpty() ? "product" : input;
  }

  @Override
  public ProductResDTO convertToProductResDTO(Product product) {
    return ProductResDTO.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .minPrice(promotionCalculatorService.calculateMinPrice(product))
        .maxPrice(promotionCalculatorService.calculateMaxPrice(product))
        .priceWithDiscount(promotionCalculatorService.calculatePriceWithDiscount(product))
        .minPriceWithDiscount(promotionCalculatorService.calculateMinPriceWithDiscount(product))
        .maxPriceWithDiscount(promotionCalculatorService.calculateMaxPriceWithDiscount(product))
        .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
        .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
        .isFeatured(product.isFeatured())
        .discountRate(promotionCalculatorService.calculateDiscountRate(product))
        .averageRating(calculateAverageRating(product) == 0 ? null : calculateAverageRating(product))
        .numberOfReviews(calculateNumberOfReviews(product))
        .numberOfSold(calculateNumberOfSold(product))
        .slug(product.getSlug())
        .colorDefault(product.getColorDefault() != null ? product.getColorDefault().name() : null)
        .images(product.getImages().stream()
            .map(ProductImage::getPublicUrl)
            .collect(Collectors.toList()))
        .variants(product.getVariants().stream()
            .map(variant -> ProductResDTO.ProductVariantResDTO.builder()
                .id(variant.getId())
                .color(variant.getColor().name())
                .size(variant.getSize().name())
                .quantity(variant.getQuantity())
                .currentUserCartQuantity(Long.valueOf(calculateVariantQuantity(variant)))
                .differencePrice(variant.getDifferencePrice())
                .images(variant.getImages().stream()
                    .map(ProductImage::getPublicUrl)
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList()))
        .build();
  }

  private Integer calculateVariantQuantity(ProductVariant variant) {
    String email = SecurityUtil.getCurrentUserLogin().orElse(null);
    if (email == null || "anonymousUser".equals(email)) {
        return 0;
    }
    
    List<CartItem> cartItems = cartRepository.findByUserEmail(email)
        .map(Cart::getCartItems)
        .orElse(null);
        
    if (cartItems == null) {
        return 0;
    }
    
    return cartItems.stream()
        .filter(item -> item.getProductVariant() != null 
            && item.getProductVariant().getId() != null
            && item.getProductVariant().getId().equals(variant.getId()))
        .map(CartItem::getQuantity)
        .findFirst()
        .orElse(0);
  }

  private Double calculateAverageRating(Product product) {
    if (product == null) {
      return 0.0;
    }
    return reviewRepository.calculateAverageRatingByProductId(product.getId());
  }

  @Override
  @EnableSoftDeleteFilter
  public ResultPaginationDTO getReviewsByProductSlug(String slug, Integer rating,
      Pageable pageable) {
    if (slug == null) {
      throw new ResourceNotFoundException(ErrorMessage.PRODUCT_NOT_FOUND);
    }

    Specification<Review> specification = Specification
        .where(ReviewSpecification.hasProductSlug(slug)).and(ReviewSpecification.isPublished())
        .and(ReviewSpecification.isNotDeleted()).and(ReviewSpecification.hasRating(rating));

    Page<Review> reviews = reviewRepository.findAll(specification, pageable);

    return ResultPaginationDTO.builder()
        .meta(Meta.builder().page((long) reviews.getNumber()).pageSize((long) reviews.getSize())
            .pages((long) reviews.getTotalPages()).total(reviews.getTotalElements()).build())
        .data(reviews.getContent().stream().map(review -> {
          if (review == null) {
            return null;
          }

          ReviewProductDTO.BoughtVariantDTO variantDTO = null;
          if (review.getLineItem() != null && review.getLineItem().getProductVariant() != null) {
            var variant = review.getLineItem().getProductVariant();
            variantDTO = ReviewProductDTO.BoughtVariantDTO.builder().variantId(variant.getId())
                .color(variant.getColor() != null ? variant.getColor().name() : null)
                .size(variant.getSize() != null ? variant.getSize().name() : null).build();
          }

          // Extract user information
          String firstName = null;
          String lastName = null;
          String avatar = null;
          if (review.getUser() != null) {
            firstName = review.getUser().getProfile().getFirstName();
            lastName = review.getUser().getProfile().getLastName();
            avatar = review.getUser().getProfile().getAvatar();
          }

          List<String> imageUrls = null;
          if (review.getImageUrls() != null) {
            imageUrls = Arrays.asList(review.getImageUrls().split(";"));
          }

          return ReviewProductDTO.builder().reviewId(review.getId()).firstName(firstName)
              .lastName(lastName).avatar(avatar).rating(review.getRating())
              .description(review.getDescription())
              .createdAt(review.getCreatedAt() != null
                  ? review.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDateTime()
                  : null)
              .variant(variantDTO).description(review.getDescription()).imageUrls(imageUrls)
              .videoUrl(review.getVideoUrl()).build();
        }).filter(dto -> dto != null).collect(Collectors.toList())).build();
  }

  private Long calculateNumberOfReviews(Product product) {
    if (product == null) {
      return 0L;
    }
    return reviewRepository.countPublishedReviewsByProductId(product.getId());
  }

  private Long calculateNumberOfSold(Product product) {
    if (product == null) {
      return 0L;
    }
    return productRepository.countSoldQuantityByProductId(product.getId(), PaymentStatus.SUCCESS);
  }

}
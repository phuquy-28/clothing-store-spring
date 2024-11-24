package com.example.clothingstore.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.ProductReqDTO;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO.Meta;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductImage;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.Promotion;
import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.enumeration.Size;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.CategoryRepository;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.service.CloudStorageService;
import com.example.clothingstore.service.ProductService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final CloudStorageService cloudStorageService;

  private final ProductRepository productRepository;

  private final CategoryRepository categoryRepository;

  @Override
  public UploadImageResDTO createSignedUrl(UploadImageReqDTO uploadImageReqDTO) {
    return cloudStorageService.createSignedUrl(uploadImageReqDTO);
  }

  @Override
  public ProductResDTO createProduct(ProductReqDTO productReqDTO) {
    Product product = new Product();
    product.setName(productReqDTO.getName());
    product.setDescription(productReqDTO.getDescription());
    product.setPrice(productReqDTO.getPrice());
    product.setCategory(categoryRepository.findById(productReqDTO.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND)));

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
                productImage.setImageOrder(index);
                productImage.setProductVariant(productVariant);
                return productImage;
            }).collect(Collectors.toList());
        productVariant.setImages(variantImages);

        return productVariant;
    }).collect(Collectors.toList()));

    product = productRepository.save(product);
    log.debug("Product created with id: {}", product.getId());

    String slug = createSlug(product.getName()) + "-" + product.getId();
    product.setSlug(slug);
    log.debug("Slug created: {}", slug);

    product = productRepository.save(product);

    return convertToProductResDTO(product);
  }

  @Override
  public ProductResDTO getProductBySlug(String slug) {
    Product product = productRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PRODUCT_NOT_FOUND));
    return convertToProductResDTO(product);
  }

  @Override
  public ResultPaginationDTO  getProducts(Specification<Product> specification, Pageable pageable) {
    Page<Product> products = productRepository.findAll(specification, pageable);
    return ResultPaginationDTO.builder()
        .meta(Meta.builder().page((long) products.getNumber()).pageSize((long) products.getSize())
            .pages((long) products.getTotalPages()).total(products.getTotalElements()).build())
        .data(products.getContent().stream().map(this::convertToProductResDTO)
            .collect(Collectors.toList()))
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
  public ProductResDTO getProductById(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PRODUCT_NOT_FOUND));
    return convertToProductResDTO(product);
  }

  private String createSlug(String input) {
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
        .categoryId(product.getCategory().getId())
        .isFeatured(product.isFeatured())
        .discountRate(calculateDiscountRate(product))
        .images(product.getImages().stream()
            .map(ProductImage::getPublicUrl)
            .collect(Collectors.toList()))
        .variants(product.getVariants().stream()
            .map(variant -> ProductResDTO.ProductVariantResDTO.builder()
                .id(variant.getId())
                .color(variant.getColor().name())
                .size(variant.getSize().name())
                .quantity(variant.getQuantity())
                .differencePrice(variant.getDifferencePrice())
                .images(variant.getImages().stream()
                    .map(ProductImage::getPublicUrl)
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public ResultPaginationDTO getBestSellerProducts(Integer days, Pageable pageable) {
    Page<Product> bestSellers;

    if (days != null) {
      LocalDateTime startDate = LocalDateTime.now().minusDays(days);
      bestSellers = productRepository.findBestSellerProducts(PaymentStatus.SUCCESS,
          startDate.toInstant(ZoneOffset.UTC), pageable);
    } else {
      bestSellers = productRepository.findBestSellerProducts(PaymentStatus.SUCCESS, pageable);
    }

    return ResultPaginationDTO.builder()
        .meta(ResultPaginationDTO.Meta.builder().page((long) bestSellers.getNumber())
            .pageSize((long) bestSellers.getSize()).pages((long) bestSellers.getTotalPages())
            .total(bestSellers.getTotalElements()).build())
        .data(bestSellers.getContent().stream().map(this::convertToProductResDTO)
            .collect(Collectors.toList()))
        .build();
  }

  private Double calculateDiscountRate(Product product) {
    Instant now = Instant.now();

    Double maxProductDiscount = product.getPromotions().stream()
        .filter(promotion -> promotion.getStartDate().isBefore(now)
            && promotion.getEndDate().isAfter(now))
        .map(Promotion::getDiscountRate).max(Double::compare).orElse(0.0);

    Double maxCategoryDiscount = product.getCategory().getPromotions().stream()
        .filter(promotion -> promotion.getStartDate().isBefore(now)
            && promotion.getEndDate().isAfter(now))
        .map(Promotion::getDiscountRate).max(Double::compare).orElse(0.0);

    return Math.max(maxProductDiscount, maxCategoryDiscount);
  }

  @Override
  public ResultPaginationDTO getDiscountedProducts(Pageable pageable) {
    Page<Product> discountedProducts = productRepository.findDiscountedProducts(Instant.now(), pageable);

    return ResultPaginationDTO.builder()
        .meta(Meta.builder()
            .page((long) discountedProducts.getNumber())
            .pageSize((long) discountedProducts.getSize())
            .pages((long) discountedProducts.getTotalPages())
            .total(discountedProducts.getTotalElements())
            .build())
        .data(discountedProducts.getContent().stream()
            .map(this::convertToProductResDTO)
            .collect(Collectors.toList()))
        .build();
  }
}
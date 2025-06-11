package com.example.clothingstore.service.impl;

import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.entity.*;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.repository.UserProfileVectorRepository;
import com.example.clothingstore.repository.ProductEmbeddingRepository;
import com.example.clothingstore.service.PineconeService;
import com.example.clothingstore.service.ProductService;
import com.example.clothingstore.service.RecommendationService;
import com.example.clothingstore.enumeration.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final ProductRepository productRepository;
  private final ProductService productService;
  private final PineconeService pineconeService;
  private final UserProfileVectorRepository userProfileVectorRepository;
  private final ProductEmbeddingRepository productEmbeddingRepository;
  private final ObjectMapper objectMapper;

  @Override
  public List<ProductResDTO> getRecommendationsForUser(User user, int limit) {
    // Kiểm tra user null
    if (user == null) {
      log.warn("User is null. Returning top selling products.");
      return getTopSellingProducts(limit);
    }

    // Lấy vector hồ sơ người dùng (từ cache trong MySQL hoặc tính toán mới)
    List<Float> userProfileVector = getOrCreateUserProfileVector(user);

    if (userProfileVector == null || userProfileVector.isEmpty()) {
      log.warn("Could not generate profile vector for user {}. Returning top selling products.",
          user.getEmail());
      return getTopSellingProducts(limit);
    }

    // Lấy lịch sử mua hàng để lọc ra các sản phẩm đã tương tác
    Set<Long> userHistoryProductIds = getUserHistoryProductIds(user);

    // Truy vấn Pinecone để lấy các ID sản phẩm tương tự
    List<Long> recommendedIds = pineconeService.querySimilarVectors(userProfileVector,
        limit + userHistoryProductIds.size());

    // Lọc bỏ các sản phẩm đã mua và lấy đủ số lượng `limit`
    List<Long> finalIds = recommendedIds.stream().filter(id -> !userHistoryProductIds.contains(id))
        .limit(limit).collect(Collectors.toList());

    // Lấy thông tin sản phẩm từ DB và trả về
    return productRepository.findAllById(finalIds).stream()
        .sorted(Comparator.comparing(p -> finalIds.indexOf(p.getId()))) // Giữ đúng thứ tự đề xuất
                                                                        // từ Pinecone
        .map(productService::convertToProductResDTO).collect(Collectors.toList());
  }

  private List<Float> getOrCreateUserProfileVector(User user) {
    Optional<UserProfileVector> cachedOpt = userProfileVectorRepository.findByUserId(user.getId());

    // Kiểm tra cache có tồn tại và còn "tươi" (dưới 24 giờ) không
    if (cachedOpt.isPresent()
        && cachedOpt.get().getUpdatedAt().isAfter(Instant.now().minus(24, ChronoUnit.HOURS))) {
      log.info("Cache hit for user profile vector from MySQL: {}", user.getEmail());
      try {
        List<Number> numbers = objectMapper.readValue(cachedOpt.get().getVector(),
            new TypeReference<List<Number>>() {});
        return numbers.stream().map(Number::floatValue).collect(Collectors.toList());
      } catch (IOException e) {
        log.error("Error deserializing cached vector for user {}", user.getId(), e);
      }
    }

    log.info("Cache miss or stale for user profile vector in MySQL: {}", user.getEmail());
    Set<Long> historyIds = getUserHistoryProductIds(user);
    if (historyIds.isEmpty()) {
      return Collections.emptyList();
    }

    List<Float> newVector = createUserProfileVector(historyIds);
    if (!newVector.isEmpty()) {
      saveOrUpdateUserProfileVector(user, newVector);
    }
    return newVector;
  }

  @Transactional
  private void saveOrUpdateUserProfileVector(User user, List<Float> vector) {
    if (user == null || user.getId() == null) {
      log.error("Cannot save user profile vector: user or user ID is null");
      return;
    }

    try {
      String vectorAsJson = objectMapper.writeValueAsString(vector);

      // Tìm kiếm UserProfileVector hiện có
      Optional<UserProfileVector> existingOpt =
          userProfileVectorRepository.findByUserId(user.getId());

      UserProfileVector userProfileVector;
      if (existingOpt.isPresent()) {
        // Cập nhật entity hiện có
        userProfileVector = existingOpt.get();
      } else {
        // Tạo entity mới với User (quan trọng với @MapsId)
        userProfileVector = new UserProfileVector(user);
        // userId sẽ tự động được map từ User thông qua @MapsId
      }

      userProfileVector.setVector(vectorAsJson);
      userProfileVector.setUpdatedAt(Instant.now());

      userProfileVectorRepository.save(userProfileVector);
      log.info("Saved/Updated user profile vector in MySQL for user: {}", user.getEmail());
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize user profile vector for user {}: {}", user.getId(), e);
    } catch (Exception e) {
      log.error("Failed to save user profile vector for user {}: {}", user.getId(), e.getMessage());
    }
  }

  private List<Float> createUserProfileVector(Set<Long> productIds) {
    if (productIds.isEmpty()) {
      return Collections.emptyList();
    }

    List<List<Float>> vectors = new ArrayList<>();
    try {
      // Lấy tất cả embeddings một lần bằng query tối ưu
      List<ProductEmbedding> embeddings =
          productEmbeddingRepository.findAllByProductIds(productIds);

      // Chuyển đổi các vector từ JSON string sang List<Float>
      for (ProductEmbedding embedding : embeddings) {
        List<Number> numbers =
            objectMapper.readValue(embedding.getVector(), new TypeReference<List<Number>>() {});
        List<Float> floats = numbers.stream().map(Number::floatValue).collect(Collectors.toList());
        vectors.add(floats);
      }

      if (vectors.isEmpty()) {
        return Collections.emptyList();
      }

      // Tính vector trung bình
      int vecSize = vectors.get(0).size();
      List<Float> averageVector = new ArrayList<>(Collections.nCopies(vecSize, 0.0f));
      for (List<Float> vector : vectors) {
        for (int i = 0; i < vecSize; i++) {
          averageVector.set(i, averageVector.get(i) + vector.get(i));
        }
      }
      for (int i = 0; i < vecSize; i++) {
        averageVector.set(i, averageVector.get(i) / vectors.size());
      }
      return averageVector;

    } catch (Exception e) {
      log.error("Error processing embeddings for user profile: {}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private Set<Long> getUserHistoryProductIds(User user) {
    return user.getOrders().stream().flatMap(order -> order.getLineItems().stream())
        // Nhóm theo sản phẩm và tính tổng số lượng đã mua
        .collect(Collectors.groupingBy(item -> item.getProductVariant().getProduct().getId(),
            Collectors.summingLong(LineItem::getQuantity)))
        // Chuyển thành Stream các Entry để sắp xếp
        .entrySet().stream()
        // Sắp xếp giảm dần theo số lượng đã mua
        .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
        // Lấy 10 sản phẩm đầu tiên
        .limit(10)
        // Lấy ra product ID
        .map(Map.Entry::getKey).collect(Collectors.toSet());
  }

  private List<ProductResDTO> getTopSellingProducts(int limit) {
    return productRepository.findTopSellingProducts(OrderStatus.DELIVERED, PageRequest.of(0, limit))
        .stream().map(productService::convertToProductResDTO).toList();
  }

}

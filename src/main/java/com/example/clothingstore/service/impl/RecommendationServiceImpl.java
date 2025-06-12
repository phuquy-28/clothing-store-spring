package com.example.clothingstore.service.impl;

import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.entity.*;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.repository.ProductEmbeddingRepository;
import com.example.clothingstore.repository.ProductViewHistoryRepository;
import com.example.clothingstore.service.PineconeService;
import com.example.clothingstore.service.ProductService;
import com.example.clothingstore.service.RecommendationService;
import com.example.clothingstore.enumeration.OrderStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final ProductRepository productRepository;
  private final ProductService productService;
  private final PineconeService pineconeService;
  private final ProductEmbeddingRepository productEmbeddingRepository;
  private final ProductViewHistoryRepository productViewHistoryRepository;
  private final ObjectMapper objectMapper;

  @Override
  public List<ProductResDTO> getRecommendationsForUser(User user, int limit) {
    // Kiểm tra user null
    if (user == null) {
      log.warn("User is null. Returning top selling products.");
      return getTopSellingProducts(limit);
    }

    // Lấy ra các sở thích có trọng số của người dùng
    Map<Long, Double> weightedPreferences = getWeightedUserPreferences(user);

    if (weightedPreferences.isEmpty()) {
      log.warn("User {} has no history. Returning featured products.", user.getEmail());
      return getTopSellingProducts(limit);
    }

    // Tạo vector hồ sơ người dùng có trọng số
    List<Float> userProfileVector = createWeightedUserProfileVector(weightedPreferences);

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

    // Đảm bảo có đủ số lượng `limit` sản phẩm, nếu không đủ, thêm các sản phẩm đã mua vào cuối danh
    // sách
    List<Long> finalIds = new ArrayList<>();
    finalIds.addAll(recommendedIds.stream().filter(id -> !userHistoryProductIds.contains(id))
        .limit(limit).collect(Collectors.toList()));
    finalIds.addAll(userHistoryProductIds.stream().filter(id -> !finalIds.contains(id)).limit(limit)
        .collect(Collectors.toList()));

    // Lấy thông tin sản phẩm từ DB và trả về
    return productRepository.findAllById(finalIds).stream()
        .sorted(Comparator.comparing(p -> finalIds.indexOf(p.getId()))) // Giữ đúng thứ tự đề xuất
                                                                        // từ Pinecone
        .map(productService::convertToProductResDTO).collect(Collectors.toList());
  }

  private Map<Long, Double> getWeightedUserPreferences(User user) {
    Map<Long, Double> preferences = new HashMap<>();

    // 1. Lượt xem sản phẩm (lấy 10 lượt xem gần nhất) - trọng số 0.3
    List<ProductViewHistory> views =
        productViewHistoryRepository.findTop10ByUserOrderByViewedAtDesc(user);
    views.forEach(view -> preferences.put(view.getProduct().getId(), 0.5));

    // 2. Lịch sử mua hàng - trọng số 1.0 (ghi đè lên trọng số của lượt xem nếu trùng)
    user.getOrders().stream().flatMap(order -> order.getLineItems().stream())
        .map(LineItem::getProductVariant).map(ProductVariant::getProduct)
        .forEach(product -> preferences.put(product.getId(), 1.0));

    return preferences;
  }

  private List<Float> createWeightedUserProfileVector(Map<Long, Double> weightedPreferences) {
    List<List<Float>> vectors = new ArrayList<>();
    List<Double> weights = new ArrayList<>();
    double totalWeight = 0.0;

    for (Map.Entry<Long, Double> entry : weightedPreferences.entrySet()) {
      try {
        Product p = productRepository.findById(entry.getKey()).orElse(null);
        if (p != null) {
          ProductEmbedding productEmbedding =
              productEmbeddingRepository.findByProductId(p.getId()).orElse(null);
          List<Number> numbers = objectMapper.readValue(productEmbedding.getVector(),
              new TypeReference<List<Number>>() {});
          List<Float> floats =
              numbers.stream().map(Number::floatValue).collect(Collectors.toList());
          vectors.add(floats);
          weights.add(entry.getValue());
          totalWeight += entry.getValue();
        }
      } catch (Exception e) {
        log.warn("Could not create embedding for product ID {} for user profile", entry.getKey(),
            e);
      }
    }

    if (vectors.isEmpty())
      return Collections.emptyList();

    int vecSize = vectors.get(0).size();
    List<Float> weightedAverageVector = new ArrayList<>(Collections.nCopies(vecSize, 0.0f));

    for (int i = 0; i < vectors.size(); i++) {
      List<Float> vector = vectors.get(i);
      double weight = weights.get(i);
      for (int j = 0; j < vecSize; j++) {
        // Cộng dồn vector đã được nhân với trọng số
        weightedAverageVector.set(j,
            (float) (weightedAverageVector.get(j) + vector.get(j) * weight));
      }
    }

    if (totalWeight == 0)
      return Collections.emptyList();

    // Chia cho tổng trọng số để lấy trung bình
    for (int i = 0; i < vecSize; i++) {
      weightedAverageVector.set(i, (float) (weightedAverageVector.get(i) / totalWeight));
    }

    return weightedAverageVector;
  }

  private Set<Long> getUserHistoryProductIds(User user) {
    return user.getOrders().stream().flatMap(order -> order.getLineItems().stream())
        .map(LineItem::getProductVariant).map(ProductVariant::getProduct).map(Product::getId)
        .collect(Collectors.toSet());
  }

  private List<ProductResDTO> getTopSellingProducts(int limit) {
    return productRepository.findTopSellingProducts(OrderStatus.DELIVERED, PageRequest.of(0, limit))
        .stream().map(productService::convertToProductResDTO).toList();
  }

}

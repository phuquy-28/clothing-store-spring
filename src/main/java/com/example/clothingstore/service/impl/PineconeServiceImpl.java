package com.example.clothingstore.service.impl;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.NamespaceSummary;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.ProductEmbedding;
import com.example.clothingstore.repository.ProductEmbeddingRepository;
import com.example.clothingstore.service.EmbeddingService;
import com.example.clothingstore.service.PineconeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PineconeServiceImpl implements PineconeService {

  private Index index;
  private final EmbeddingService embeddingService;
  private final ProductEmbeddingRepository productEmbeddingRepository;
  private final ObjectMapper objectMapper;

  @Value("${pinecone.api-key}")
  private String apiKey;

  @Value("${pinecone.index}")
  private String indexName;

  @Value("${pinecone.namespace}")
  private String namespace;

  @PostConstruct
  public void init() {
    Pinecone pinecone = new Pinecone.Builder(apiKey).build();
    this.index = pinecone.getIndexConnection(indexName);
    log.info("Initialized Pinecone connection to index: {}", indexName);
  }

  @Async
  @Override
  public void indexSingleProduct(Product product) {
    try {
      log.info("Asynchronously indexing product ID: {}", product.getId());
      String document = createProductDocument(product);

      // Try to get existing embedding from database
      Optional<ProductEmbedding> existingEmbedding =
          productEmbeddingRepository.findByProductId(product.getId());
      List<Float> embedding;

      if (existingEmbedding.isPresent() && existingEmbedding.get().getUpdatedAt()
          .isAfter(Instant.now().minus(24, ChronoUnit.HOURS))) {
        log.info("Using cached embedding for product ID: {}", product.getId());
        List<Double> doubleEmbedding = objectMapper.readValue(existingEmbedding.get().getVector(), 
            objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class));
        embedding = doubleEmbedding.stream()
            .map(Double::floatValue)
            .collect(Collectors.toList());
      } else {
        log.info("Creating new embedding for product ID: {}", product.getId());
        embedding = embeddingService.createEmbedding(document);

        // Save or update embedding in database
        ProductEmbedding productEmbedding = existingEmbedding.orElse(new ProductEmbedding());
        productEmbedding.setProductId(product.getId());
        productEmbedding.setVector(objectMapper.writeValueAsString(embedding));
        productEmbedding.setUpdatedAt(Instant.now());
        productEmbeddingRepository.save(productEmbedding);
      }

      upsertVector(String.valueOf(product.getId()), embedding);
      log.info("Successfully indexed product ID: {}", product.getId());
    } catch (Exception e) {
      log.error("Failed to index single product ID {}: {}", product.getId(), e.getMessage(), e);
    }
  }

  private String createProductDocument(Product product) {
    String colors = product.getVariants().stream().map(ProductVariant::getColor).distinct()
        .map(Enum::name).collect(Collectors.joining(" "));
    return product.getName() + " " + product.getCategory().getName() + " "
        + product.getDescription() + " " + colors;
  }

  @Override
  public void upsertVector(String id, List<Float> values) {
    List<VectorWithUnsignedIndices> vectors = new ArrayList<>();
    vectors.add(new VectorWithUnsignedIndices(id, values));
    index.upsert(vectors, namespace);
  }

  @Override
  public List<Long> querySimilarVectors(List<Float> queryVector, int topK) {
    QueryResponseWithUnsignedIndices response =
        index.query(topK, queryVector, null, null, null, namespace, null, true, true);
    return response.getMatchesList().stream().map(match -> Long.parseLong(match.getId()))
        .collect(Collectors.toList());
  }

  @Override
  public int getNamespaceVectorCount(String namespace) {
    try {
      DescribeIndexStatsResponse statsResponse = index.describeIndexStats();
      Map<String, NamespaceSummary> namespaces = statsResponse.getNamespacesMap();

      if (namespaces != null && namespaces.containsKey(namespace)) {
        return namespaces.get(namespace).getVectorCount();
      }

      return 0;
    } catch (Exception e) {
      log.error("Failed to get vector count for namespace '{}': {}", namespace, e.getMessage());
      return -1;
    }
  }
}

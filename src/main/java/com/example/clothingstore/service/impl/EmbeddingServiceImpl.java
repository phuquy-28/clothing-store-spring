package com.example.clothingstore.service.impl;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.clothingstore.service.EmbeddingService;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

  @Value("${openai.api-key}")
  private String openApiKey;

  private final Logger log = LoggerFactory.getLogger(EmbeddingServiceImpl.class);

  private final String modelName = "text-embedding-3-small";

  private OpenAIClient client;

  @PostConstruct
  public void init() {
    this.client = OpenAIOkHttpClient.builder().apiKey(openApiKey).build();
    log.info("Initialized OpenAI client");
  }

  @Override
  public List<Float> createEmbedding(String text) {
    try {
      log.debug("Creating embedding for text: {}", text);
      EmbeddingCreateParams params =
          EmbeddingCreateParams.builder().model(modelName).input(text).dimensions(256).build();

      CreateEmbeddingResponse response = client.embeddings().create(params);

      if (response.data() == null || response.data().isEmpty()) {
        log.info("Failed to create embedding for text: {}", text);
        throw new RuntimeException("Failed to create embedding for text: " + text);
      }

      Embedding firstEmbedding = response.data().get(0);
      List<Float> embeddingFloats = firstEmbedding.embedding();

      log.debug("Successfully created embedding with {} dimensions", embeddingFloats.size());
      return embeddingFloats;

    } catch (Exception e) {
      log.info("Error while calling OpenAI embedding API: {}", e.getMessage(), e);
      throw new RuntimeException("Error while calling OpenAI embedding API", e);
    }
  }
}

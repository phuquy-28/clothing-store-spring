package com.example.clothingstore.service;

import java.util.List;

public interface EmbeddingService {
  List<Float> createEmbedding(String text);
}

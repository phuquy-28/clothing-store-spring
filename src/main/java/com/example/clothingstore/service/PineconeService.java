package com.example.clothingstore.service;

import com.example.clothingstore.entity.Product;
import java.util.List;

public interface PineconeService {

  void indexSingleProduct(Product product);

  void upsertVector(String id, List<Float> values);

  List<Long> querySimilarVectors(List<Float> queryVector, int topK);

  int getNamespaceVectorCount(String namespace);
}

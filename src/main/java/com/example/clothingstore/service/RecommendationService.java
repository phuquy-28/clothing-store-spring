package com.example.clothingstore.service;

import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.entity.User;
import java.util.List;

public interface RecommendationService {

  List<ProductResDTO> getRecommendationsForUser(User user, int limit);

}

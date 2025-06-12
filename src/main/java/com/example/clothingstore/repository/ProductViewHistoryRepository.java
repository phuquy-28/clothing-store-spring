package com.example.clothingstore.repository;

import com.example.clothingstore.entity.ProductViewHistory;
import com.example.clothingstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductViewHistoryRepository extends JpaRepository<ProductViewHistory, Long> {

  List<ProductViewHistory> findTop10ByUserOrderByViewedAtDesc(User user);

}

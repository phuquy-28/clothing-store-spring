package com.example.clothingstore.repository;

import com.example.clothingstore.entity.ProductViewHistory;
import com.example.clothingstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface ProductViewHistoryRepository extends JpaRepository<ProductViewHistory, Long> {

  List<ProductViewHistory> findTop10ByUserOrderByViewedAtDesc(User user);

  @Query("""
      SELECT COUNT(pvh)
      FROM ProductViewHistory pvh
      WHERE pvh.viewedAt BETWEEN :startDate AND :endDate
      """)
  Long countTotalViewsByViewedAtBetween(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

}

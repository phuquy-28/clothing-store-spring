package com.example.clothingstore.repository;

import com.example.clothingstore.entity.PointHistory;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.PointActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointHistoryRepository
    extends JpaRepository<PointHistory, Long>, JpaSpecificationExecutor<PointHistory> {
  List<PointHistory> findByUserOrderByCreatedAtDesc(User user);

  Optional<PointHistory> findByOrderIdAndActionType(Long orderId, PointActionType actionType);
}

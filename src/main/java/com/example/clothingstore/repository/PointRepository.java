package com.example.clothingstore.repository;

import com.example.clothingstore.entity.Point;
import com.example.clothingstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUser(User user);
    Optional<Point> findByUserId(Long userId);
} 
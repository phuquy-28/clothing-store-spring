package com.example.clothingstore.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.clothingstore.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  Optional<Review> findByLineItemId(Long lineItemId);
}

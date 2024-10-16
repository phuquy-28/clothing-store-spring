package com.example.clothingstore.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.clothingstore.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByCode(String code);

}

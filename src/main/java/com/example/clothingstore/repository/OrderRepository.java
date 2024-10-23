package com.example.clothingstore.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.User;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByCode(String code);

  List<Order> findByUser(User user);

}

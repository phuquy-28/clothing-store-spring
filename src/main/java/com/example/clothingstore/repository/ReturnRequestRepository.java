package com.example.clothingstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.ReturnRequest;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.ReturnRequestStatus;

@Repository
public interface ReturnRequestRepository
    extends JpaRepository<ReturnRequest, Long>, JpaSpecificationExecutor<ReturnRequest> {

  List<ReturnRequest> findByUser(User user);

  List<ReturnRequest> findByStatus(ReturnRequestStatus status);

  Optional<ReturnRequest> findByOrder(Order order);

  boolean existsByOrder(Order order);
}

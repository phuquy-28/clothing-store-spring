package com.example.clothingstore.repository;

import com.example.clothingstore.entity.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryHistoryRepository
    extends JpaRepository<InventoryHistory, Long>, JpaSpecificationExecutor<InventoryHistory> {
}

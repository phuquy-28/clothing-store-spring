package com.example.clothingstore.service;

import com.example.clothingstore.dto.response.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.entity.InventoryHistory;
import org.springframework.core.io.Resource;

public interface InventoryHistoryService {
  ResultPaginationDTO getAllInventoryHistory(Specification<InventoryHistory> spec,
      Pageable pageable);

  Resource exportInventoryHistory(Specification<InventoryHistory> spec);
}

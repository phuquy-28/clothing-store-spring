package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResDTO {

  private Long totalUsers;

  private Long totalOrders;

  private Long totalRevenue;

  private Long totalProducts;
}

package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.DashboardResDTO;
import com.example.clothingstore.dto.response.DashboardSummaryDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.RevenueByMonth;
import com.example.clothingstore.dto.response.RevenueChartDTO;

public interface WorkspaceService {
  
  LoginResDTO login(LoginReqDTO loginReqDTO);

  DashboardResDTO getDashboard();

  RevenueByMonth getRevenueByMonth(Long year);

  DashboardSummaryDTO getDashboardSummary(String period);

  RevenueChartDTO getRevenueChart(Long year);
}
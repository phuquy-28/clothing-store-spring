package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.DashboardResDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.RevenueByMonth;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.exception.BadCredentialsException;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.service.WorkspaceService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

  private final Logger log = LoggerFactory.getLogger(WorkspaceServiceImpl.class);

  private final AuthenticationManager authenticationManager;

  private final UserService userService;

  private final SecurityUtil securityUtil;

  private final OrderRepository orderRepository;

  private final ProductRepository productRepository;

  @Override
  public LoginResDTO login(LoginReqDTO loginReqDto) {
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
          loginReqDto.getEmail(), loginReqDto.getPassword()));
    } catch (AuthenticationException e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new BadCredentialsException(ErrorMessage.USERNAME_OR_PASSWORD_INVALID);
    }

    // Get user information
    User loginUser = userService.handleGetUserByUsername(loginReqDto.getEmail());

    // Check if user has admin role (not USER role)
    if (AppConstant.ROLE_USER.equalsIgnoreCase(loginUser.getRole().getName())) {
      throw new BadCredentialsException(ErrorMessage.USERNAME_OR_PASSWORD_INVALID);
    }

    // Create response
    LoginResDTO loginResDTO = AuthServiceImpl.convertUserToResLoginDTO(loginUser);

    if (loginUser.isActivated()) {
      String accessToken = securityUtil.createAccessToken(loginUser, loginResDTO);
      loginResDTO.setAccessToken(accessToken);

      String refreshToken = securityUtil.createRefreshToken(loginReqDto.getEmail(), loginResDTO);
      loginResDTO.setRefreshToken(refreshToken);

      userService.updateUserWithRefreshToken(loginUser, refreshToken);
    }

    return loginResDTO;
  }

  @Override
  public DashboardResDTO getDashboard() {
    log.debug("Request to get dashboard statistics");

    // Get total users (only activated accounts)
    Long totalUsers = userService.countActivatedUsers();
    
    // Get total orders
    Long totalOrders = orderRepository.count();
    
    // Calculate total revenue from completed orders
    Long totalRevenue = orderRepository.sumFinalTotalByStatus(OrderStatus.DELIVERED);
    
    // Get total products (not deleted)
    Long totalProducts = productRepository.countByIsDeletedFalse();

    return DashboardResDTO.builder().totalUsers(totalUsers).totalOrders(totalOrders)
        .totalRevenue(totalRevenue).totalProducts(totalProducts).build();
  }

  @Override
  public RevenueByMonth getRevenueByMonth(Long year) {
    log.debug("Request to get revenue by month for year: {}", year);

    if (year == null) {
      year = (long) LocalDateTime.now().getYear();
    }

    List<Object[]> results = orderRepository.findRevenueByMonth(year, OrderStatus.DELIVERED);

    List<RevenueByMonth.RevenueByMonthDTO> revenueByMonthDTOs = new ArrayList<>();

    Map<Integer, Double> revenueMap = new HashMap<>();
    for (Object[] result : results) {
      int month = ((Number) result[0]).intValue();
      double revenue = Math.round(((Number) result[1]).doubleValue() * 100.0) / 100.0;
      revenueMap.put(month, revenue);
    }

    // Đảm bảo có đủ 12 tháng, tháng nào không có doanh thu thì set = 0
    for (int month = 1; month <= 12; month++) {
      double revenue = revenueMap.getOrDefault(month, 0.0);
      revenueByMonthDTOs
          .add(RevenueByMonth.RevenueByMonthDTO.builder().month(month).revenue(revenue).build());
    }

    revenueByMonthDTOs.sort((a, b) -> Integer.compare(a.getMonth(), b.getMonth()));

    return RevenueByMonth.builder().revenueByMonth(revenueByMonthDTOs).build();
  }

}

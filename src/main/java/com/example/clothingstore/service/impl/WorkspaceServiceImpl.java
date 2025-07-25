package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.CategorySalesDTO;
import com.example.clothingstore.dto.response.DashboardResDTO;
import com.example.clothingstore.dto.response.DashboardSummaryDTO;
import com.example.clothingstore.dto.response.DashboardSummaryDTO.MetricDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.ProductPerformanceDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.RevenueByMonth;
import com.example.clothingstore.dto.response.RevenueChartDTO;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.ReturnRequestStatus;
import com.example.clothingstore.exception.BadCredentialsException;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.service.WorkspaceService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.repository.ProductViewHistoryRepository;
import com.example.clothingstore.repository.ReturnRequestRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

  private final Logger log = LoggerFactory.getLogger(WorkspaceServiceImpl.class);

  private final AuthenticationManager authenticationManager;

  private final UserService userService;

  private final SecurityUtil securityUtil;

  private final OrderRepository orderRepository;

  private final ProductRepository productRepository;

  private final ProductViewHistoryRepository productViewHistoryRepository;

  private final ReturnRequestRepository returnRequestRepository;

  private record DateRanges(Instant currentStart, Instant currentEnd, Instant previousStart,
      Instant previousEnd) {
  }

  @Override
  public LoginResDTO login(LoginReqDTO loginReqDto) {
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
          loginReqDto.getEmail(), loginReqDto.getPassword()));
    } catch (AuthenticationException e) {
      log.info("Authentication failed: {}", e.getMessage());
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

  @Override
  public DashboardSummaryDTO getDashboardSummary(String period) {
    DateRanges ranges = calculateComparisonRanges(period);

    // Fetch Total Sales
    double currentSales = orderRepository.sumFinalTotalByStatusAndOrderDateBetween(
        OrderStatus.DELIVERED, ranges.currentStart(), ranges.currentEnd());
    double previousSales = orderRepository.sumFinalTotalByStatusAndOrderDateBetween(
        OrderStatus.DELIVERED, ranges.previousStart(), ranges.previousEnd());

    // Fetch Total Orders
    long currentOrders =
        orderRepository.countByOrderDateBetween(ranges.currentStart(), ranges.currentEnd());
    long previousOrders =
        orderRepository.countByOrderDateBetween(ranges.previousStart(), ranges.previousEnd());

    // Fetch Visitors
    long currentViews = productViewHistoryRepository
        .countTotalViewsByViewedAtBetween(ranges.currentStart(), ranges.currentEnd());
    long previousViews = productViewHistoryRepository
        .countTotalViewsByViewedAtBetween(ranges.previousStart(), ranges.previousEnd());

    // Fetch Refunded Orders
    long currentRefunded = returnRequestRepository.countByStatusAndCreatedAtBetween(
        ReturnRequestStatus.APPROVED, ranges.currentStart(), ranges.currentEnd());
    long previousRefunded = returnRequestRepository.countByStatusAndCreatedAtBetween(
        ReturnRequestStatus.APPROVED, ranges.previousStart(), ranges.previousEnd());


    return DashboardSummaryDTO.builder()
        .totalSales(
            new MetricDTO(currentSales, calculatePercentageChange(currentSales, previousSales)))
        .totalOrders(new MetricDTO((double) currentOrders,
            calculatePercentageChange(currentOrders, previousOrders)))
        .visitors(new MetricDTO((double) currentViews,
            calculatePercentageChange(currentViews, previousViews)))
        .refunded(new MetricDTO((double) currentRefunded,
            calculatePercentageChange(currentRefunded, previousRefunded)))
        .build();
  }

  private double calculatePercentageChange(double current, double previous) {
    if (previous == 0) {
      return 0.0; // Return 100% if previous is 0 and current is positive
    }
    return Math.round(((current - previous) / previous) * 100.0);
  }

  private DateRanges calculateComparisonRanges(String period) {
    Instant now = Instant.now();
    ZonedDateTime zdtNow = ZonedDateTime.ofInstant(now, ZoneId.of("UTC"));

    Instant currentStart, currentEnd, previousStart, previousEnd;

    switch (period.toLowerCase()) {
      case "today":
        currentStart = zdtNow.truncatedTo(ChronoUnit.DAYS).toInstant();
        currentEnd = now;

        previousStart = currentStart.minus(1, ChronoUnit.DAYS);
        previousEnd = now.minus(1, ChronoUnit.DAYS);
        break;

      case "this_month":
        currentStart = zdtNow.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant();
        currentEnd = now;

        ZonedDateTime previousMonth = zdtNow.minusMonths(1);
        previousStart = previousMonth.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant();
        previousEnd = previousMonth.withDayOfMonth(zdtNow.getDayOfMonth())
            .with(zdtNow.toLocalTime()).toInstant();
        break;

      case "this_week":
      default:
        currentStart = zdtNow.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .truncatedTo(ChronoUnit.DAYS).toInstant();
        currentEnd = now;

        previousStart = currentStart.minus(7, ChronoUnit.DAYS);
        previousEnd = now.minus(7, ChronoUnit.DAYS);
        break;
    }

    return new DateRanges(currentStart, currentEnd, previousStart, previousEnd);
  }

  @Override
  public RevenueChartDTO getRevenueChart(Long year) {
    log.debug("Request to get revenue and order chart for year: {}", year);

    if (year == null) {
      year = (long) LocalDateTime.now().getYear();
    }

    // 1. Fetch raw data from repository
    List<Object[]> revenueResults =
        orderRepository.findRevenueByMonthWithoutShippingFee(year, OrderStatus.DELIVERED);
    List<Object[]> orderCountResults = orderRepository.findOrderCountByMonth(year);


    // 2. Process data into maps for easy lookup
    Map<Integer, Double> revenueMap =
        revenueResults.stream().collect(Collectors.toMap(res -> ((Number) res[0]).intValue(),
            res -> Math.round(((Number) res[1]).doubleValue() * 100.0) / 100.0));

    Map<Integer, Long> orderCountMap = orderCountResults.stream().collect(Collectors
        .toMap(res -> ((Number) res[0]).intValue(), res -> ((Number) res[1]).longValue()));

    // 3. Prepare data lists for all 12 months
    List<Double> revenueData = new ArrayList<>();
    List<Long> orderCountData = new ArrayList<>();

    for (int month = 1; month <= 12; month++) {
      revenueData.add(revenueMap.getOrDefault(month, 0.0));
      orderCountData.add(orderCountMap.getOrDefault(month, 0L));
    }

    // 4. Build the DTO for the response
    List<String> labels =
        IntStream.rangeClosed(1, 12).mapToObj(m -> "Tháng " + m).collect(Collectors.toList());

    RevenueChartDTO.DatasetDTO revenueDataset =
        RevenueChartDTO.DatasetDTO.builder().label("Doanh thu").data(revenueData).build();

    RevenueChartDTO.DatasetDTO ordersDataset =
        RevenueChartDTO.DatasetDTO.builder().label("Đơn hàng").data(orderCountData).build();

    return RevenueChartDTO.builder().labels(labels)
        .datasets(Arrays.asList(revenueDataset, ordersDataset)).build();
  }

  @Override
  public List<CategorySalesDTO> getSalesByCategory(String period) {
    log.debug("Request to get sales by category for period: {}", period);

    // Reuse the same logic to calculate the date range
    DateRanges ranges = calculateComparisonRanges(period);

    // Fetch the aggregated data from the repository
    List<Object[]> results =
        orderRepository.findSalesByCategoryBetween(ranges.currentStart(), ranges.currentEnd());

    // Map the raw results to the DTO list
    return results.stream()
        .map(result -> new CategorySalesDTO((String) result[0],
            Math.round(((Number) result[1]).doubleValue() * 100.0) / 100.0))
        .collect(Collectors.toList());
  }

  @Override
  public ResultPaginationDTO getTopProducts(String period, String search, Pageable pageable) {
    log.debug("Request to get top products for period: {}, search: {}", period, search);

    DateRanges ranges = calculateComparisonRanges(period);

    Page<Object[]> resultsPage = productRepository.findTopSellingProducts(ranges.currentStart(),
        ranges.currentEnd(), search, pageable);

    List<ProductPerformanceDTO> dtoList = resultsPage.getContent().stream()
        .map(res -> ProductPerformanceDTO.builder().productId(((Number) res[0]).longValue())
            .productName((String) res[1]).imageUrl((String) res[2])
            .quantitySold(((Number) res[3]).longValue())
            .totalSales(Math.round(((Number) res[4]).doubleValue() * 100.0) / 100.0).build())
        .collect(Collectors.toList());

    ResultPaginationDTO.Meta meta = ResultPaginationDTO.Meta.builder()
        .page((long) resultsPage.getNumber()).pageSize((long) resultsPage.getSize())
        .pages((long) resultsPage.getTotalPages()).total(resultsPage.getTotalElements()).build();

    return ResultPaginationDTO.builder().meta(meta).data(dtoList).build();
  }

}

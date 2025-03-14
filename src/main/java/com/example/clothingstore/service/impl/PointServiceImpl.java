package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.RoleResDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.PointHistoryReqDTO;
import com.example.clothingstore.dto.response.PointHistoryDTO;
import com.example.clothingstore.dto.response.PointResDTO;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Point;
import com.example.clothingstore.entity.PointHistory;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.Gender;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.enumeration.PointActionType;
import com.example.clothingstore.repository.PointHistoryRepository;
import com.example.clothingstore.repository.PointRepository;
import com.example.clothingstore.service.PointService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

  private static final Double POINT_EARNING_RATE = 10000.0;

  private static final Double POINT_REDEMPTION_RATE = 100.0;

  private final Logger log = LoggerFactory.getLogger(PointServiceImpl.class);

  private final PointRepository pointRepository;

  private final PointHistoryRepository pointHistoryRepository;

  private final UserService userService;

  @Override
  public void addPointsFromOrder(Order order) {
    // Chỉ cộng điểm khi đơn hàng đã giao thành công và thanh toán thành công
    if (order.getStatus() != OrderStatus.DELIVERED
        || order.getPaymentStatus() != PaymentStatus.SUCCESS) {
      return;
    }

    // Nếu đơn hàng đã được cộng điểm rồi thì không cộng nữa
    if (order.getPointsEarned() != null && order.getPointsEarned() > 0) {
      return;
    }

    User user = order.getUser();
    Point point = user.getPoint();
    if (point == null) {
      point = new Point();
      point.setCurrentPoints(0L);
      point.setTotalAccumulatedPoints(0L);
      // Thiết lập mối quan hệ hai chiều
      point.setUser(user);
      user.setPoint(point);
      // Lưu point trước
      pointRepository.save(point);
    }

    // Tính số điểm được cộng từ tổng tiền đơn hàng
    Double orderSubtotal = order.getTotal() - order.getDiscount();
    if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
      orderSubtotal -= calculateAmountFromPoints(order.getPointsUsed());
    }

    Long pointsToAdd = calculatePointsFromAmount(orderSubtotal);

    // Cập nhật điểm cho người dùng
    point.setCurrentPoints(point.getCurrentPoints() + pointsToAdd);
    point.setTotalAccumulatedPoints(point.getTotalAccumulatedPoints() + pointsToAdd);

    // Lưu point thay vì lưu user
    pointRepository.save(point);

    // Lưu lịch sử cộng điểm
    PointHistory pointHistory = new PointHistory();
    pointHistory.setPoints(pointsToAdd);
    pointHistory.setActionType(PointActionType.EARNED);
    pointHistory.setDescription(
        String.format("Cộng %d điểm từ đơn hàng #%s", pointsToAdd, order.getCode()));
    pointHistory.setUser(user);
    pointHistory.setOrder(order);
    pointHistoryRepository.save(pointHistory);

    // Cập nhật số điểm đã cộng vào đơn hàng
    order.setPointsEarned(pointsToAdd);
    order.getPointHistories().add(pointHistory);

    log.debug("Đã cộng {} điểm cho người dùng {} từ đơn hàng #{}", pointsToAdd, user.getEmail(),
        order.getId());
  }

  @Override
  public void refundPointsFromOrder(Order order) {
    User user = order.getUser();
    if (user == null) {
      log.debug("Không thể hoàn điểm: Đơn hàng {} không có người dùng", order.getId());
      return;
    }

    Point point = user.getPoint();
    if (point == null) {
      log.debug("Người dùng {} không có điểm để hoàn lại", user.getEmail());
      return;
    }

    // 1. Hoàn lại điểm đã sử dụng trong đơn hàng (nếu có)
    if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
      long pointsToReturn = order.getPointsUsed();
      point.setCurrentPoints(point.getCurrentPoints() + pointsToReturn);

      // Ghi lại lịch sử hoàn điểm đã sử dụng
      PointHistory returnHistory = new PointHistory();
      returnHistory.setUser(user);
      returnHistory.setPoints(pointsToReturn);
      returnHistory.setActionType(PointActionType.REFUNDED);
      returnHistory.setDescription(String.format("Hoàn lại %d điểm đã sử dụng từ đơn hàng %s",
          pointsToReturn, order.getCode()));
      returnHistory.setOrder(order);
      pointHistoryRepository.save(returnHistory);

      order.getPointHistories().add(returnHistory);

      log.debug("Đã hoàn lại {} điểm đã sử dụng cho người dùng {} từ đơn hàng {}", pointsToReturn,
          user.getEmail(), order.getCode());
    }

    // 2. Thu hồi điểm đã cộng từ đơn hàng (nếu có)
    PointHistory earnedHistory = pointHistoryRepository
        .findByOrderIdAndActionType(order.getId(), PointActionType.EARNED).orElse(null);

    if (earnedHistory != null) {
      long pointsToRefund = earnedHistory.getPoints();
      point.setCurrentPoints(Math.max(0, point.getCurrentPoints() - pointsToRefund));
      point.setTotalAccumulatedPoints(point.getTotalAccumulatedPoints() - pointsToRefund);

      // Ghi lại lịch sử thu hồi điểm đã cộng
      PointHistory refundHistory = new PointHistory();
      refundHistory.setUser(user);
      refundHistory.setPoints(-pointsToRefund);
      refundHistory.setActionType(PointActionType.REFUNDED);
      refundHistory.setDescription(
          String.format("Thu hồi %d điểm đã cộng từ đơn hàng %s", pointsToRefund, order.getCode()));
      refundHistory.setOrder(order);
      pointHistoryRepository.save(refundHistory);
      order.getPointHistories().add(refundHistory);

      log.debug("Đã thu hồi {} điểm đã cộng cho người dùng {} từ đơn hàng {}", pointsToRefund,
          user.getEmail(), order.getCode());
    }

    // Lưu cập nhật điểm của người dùng
    pointRepository.save(point);
  }

  @Override
  public Long calculatePointsFromAmount(Double amount) {
    return (long) Math.floor(amount / POINT_EARNING_RATE);
  }

  @Override
  public Double calculateAmountFromPoints(Long points) {
    return points * POINT_REDEMPTION_RATE;
  }

  @Override
  public ResultPaginationDTO getUserPointHistory(Specification<PointHistory> spec,
      Pageable pageable) {
    // Thêm điều kiện lọc theo user hiện tại
    User currentUser =
        userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin().get());

    Specification<PointHistory> userSpec =
        (root, query, cb) -> cb.equal(root.get("user"), currentUser);

    // Kết hợp điều kiện user với các điều kiện lọc khác
    Specification<PointHistory> finalSpec = Specification.where(userSpec).and(spec);

    // Thêm điều kiện sắp xếp mặc định theo thời gian giảm dần nếu chưa có
    if (pageable.getSort().isUnsorted()) {
      pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
          Sort.by("createdAt").descending());
    }

    // Lấy dữ liệu phân trang
    Page<PointHistory> pointHistoryPage = pointHistoryRepository.findAll(finalSpec, pageable);

    // Tạo metadata cho phân trang
    ResultPaginationDTO.Meta meta = ResultPaginationDTO.Meta.builder()
        .page((long) pointHistoryPage.getNumber()).pageSize((long) pointHistoryPage.getSize())
        .pages((long) pointHistoryPage.getTotalPages()).total(pointHistoryPage.getTotalElements())
        .build();

    // Trả về kết quả
    return ResultPaginationDTO.builder().meta(meta)
        .data(convertToPointHistoryDTOs(pointHistoryPage.getContent())).build();
  }

  @Override
  public ResultPaginationDTO getPoints(Specification<Point> spec, Pageable pageable) {
    // Thêm điều kiện sắp xếp mặc định theo tổng điểm tích lũy giảm dần nếu chưa có
    if (pageable.getSort().isUnsorted()) {
      pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
          Sort.by("totalAccumulatedPoints").descending());
    }

    // Lấy dữ liệu phân trang
    Page<Point> pointPage = pointRepository.findAll(spec, pageable);

    // Chuyển đổi dữ liệu sang DTO
    List<PointResDTO> pointResDTOs = pointPage.getContent().stream().map(this::convertToPointResDTO)
        .collect(Collectors.toList());

    // Tạo metadata cho phân trang
    ResultPaginationDTO.Meta meta = ResultPaginationDTO.Meta.builder()
        .page((long) pointPage.getNumber()).pageSize((long) pointPage.getSize())
        .pages((long) pointPage.getTotalPages()).total(pointPage.getTotalElements()).build();

    // Trả về kết quả
    return ResultPaginationDTO.builder().meta(meta).data(pointResDTOs).build();
  }

  @Override
  @Transactional
  public PointHistoryDTO addPointHistory(PointHistoryReqDTO pointHistoryReqDTO) {
    // Lấy thông tin user
    User user = userService.handleGetUserByUsername(pointHistoryReqDTO.getEmailUser());
    if (user == null) {
      throw new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND);
    }

    // Kiểm tra loại hành động
    PointActionType actionType =
        PointActionType.valueOf(pointHistoryReqDTO.getActionType().toUpperCase());

    // Kiểm tra xem hành động có phải là ADDED hoặc SUBTRACTED không
    if (actionType != PointActionType.ADDED && actionType != PointActionType.SUBTRACTED) {
      throw new BadRequestException(ErrorMessage.POINT_ACTION_TYPE_INVALID);
    }

    Long points = pointHistoryReqDTO.getPoints();

    // Lấy hoặc tạo mới đối tượng Point cho user
    Point point = user.getPoint();
    if (point == null) {
      point = new Point();
      point.setCurrentPoints(0L);
      point.setTotalAccumulatedPoints(0L);
      point.setUser(user);
      user.setPoint(point);
      pointRepository.save(point);
    }

    // Cập nhật điểm dựa trên loại hành động
    if (actionType == PointActionType.ADDED) {
      // Cộng điểm
      point.setCurrentPoints(point.getCurrentPoints() + points);
      point.setTotalAccumulatedPoints(point.getTotalAccumulatedPoints() + points);
    } else if (actionType == PointActionType.SUBTRACTED) {
      // Kiểm tra xem có đủ điểm để trừ không
      if (point.getCurrentPoints() < points) {
        throw new BadRequestException(ErrorMessage.POINT_NOT_ENOUGH);
      }

      // Trừ điểm
      point.setCurrentPoints(point.getCurrentPoints() - points);
      // Không trừ tổng điểm tích lũy
    }

    // Lưu cập nhật điểm
    pointRepository.save(point);

    // Tạo lịch sử điểm
    PointHistory pointHistory = new PointHistory();
    pointHistory.setUser(user);
    pointHistory.setPoints(actionType == PointActionType.ADDED ? points : -points);
    pointHistory.setActionType(actionType);
    pointHistory.setDescription(pointHistoryReqDTO.getDescription());
    pointHistoryRepository.save(pointHistory);

    // Log hành động
    log.debug("Đã {} {} điểm cho người dùng {}. Lý do: {}",
        actionType == PointActionType.ADDED ? "cộng" : "trừ", points, user.getEmail(),
        pointHistoryReqDTO.getDescription());

    // Trả về DTO
    return convertToPointHistoryDTO(pointHistory);
  }

  private List<PointHistoryDTO> convertToPointHistoryDTOs(List<PointHistory> pointHistories) {
    return pointHistories.stream().map(this::convertToPointHistoryDTO).collect(Collectors.toList());
  }

  private PointHistoryDTO convertToPointHistoryDTO(PointHistory history) {
    return PointHistoryDTO.builder().id(history.getId()).points(history.getPoints())
        .actionType(history.getActionType().name()).description(history.getDescription())
        .orderCode(history.getOrder() != null ? history.getOrder().getCode() : null)
        .createdAt(history.getCreatedAt()).build();
  }

  private PointResDTO convertToPointResDTO(Point point) {
    User user = point.getUser();
    return PointResDTO.builder().id(point.getId()).currentPoints(point.getCurrentPoints())
        .totalAccumulatedPoints(point.getTotalAccumulatedPoints())
        .user(
            user != null
                ? UserResDTO.builder().id(user.getId()).email(user.getEmail())
                    .firstName(user.getProfile().getFirstName())
                    .lastName(user.getProfile().getLastName())
                    .fullName(user.getProfile().getFullName())
                    .birthDate(user.getProfile().getBirthDate().toString())
                    .phoneNumber(user.getProfile().getPhoneNumber())
                    .gender(Gender.valueOf(user.getProfile().getGender().name()))
                    .role(RoleResDTO.builder().id(user.getRole().getId())
                        .name(user.getRole().getName()).build())
                    .build()
                : null)
        .build();
  }

}

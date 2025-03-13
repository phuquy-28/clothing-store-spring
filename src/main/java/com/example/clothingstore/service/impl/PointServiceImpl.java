package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Point;
import com.example.clothingstore.entity.PointHistory;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.enumeration.PointActionType;
import com.example.clothingstore.repository.PointHistoryRepository;
import com.example.clothingstore.repository.PointRepository;
import com.example.clothingstore.service.PointService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

  private static final Double POINT_EARNING_RATE = 10000.0;

  private static final Double POINT_REDEMPTION_RATE = 100.0;

  private final Logger log = LoggerFactory.getLogger(PointServiceImpl.class);

  private final PointRepository pointRepository;

  private final PointHistoryRepository pointHistoryRepository;

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
    pointHistory
        .setDescription(String.format("Cộng %d điểm từ đơn hàng #%s", pointsToAdd, order.getCode()));
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
        
        log.debug("Đã hoàn lại {} điểm đã sử dụng cho người dùng {} từ đơn hàng {}", 
            pointsToReturn, user.getEmail(), order.getCode());
    }

    // 2. Thu hồi điểm đã cộng từ đơn hàng (nếu có)
    PointHistory earnedHistory = pointHistoryRepository
        .findByOrderIdAndActionType(order.getId(), PointActionType.EARNED)
        .orElse(null);

    if (earnedHistory != null) {
        long pointsToRefund = earnedHistory.getPoints();
        point.setCurrentPoints(Math.max(0, point.getCurrentPoints() - pointsToRefund));
        point.setTotalAccumulatedPoints(point.getTotalAccumulatedPoints() - pointsToRefund);

        // Ghi lại lịch sử thu hồi điểm đã cộng
        PointHistory refundHistory = new PointHistory();
        refundHistory.setUser(user);
        refundHistory.setPoints(-pointsToRefund);
        refundHistory.setActionType(PointActionType.REFUNDED);
        refundHistory.setDescription(String.format("Thu hồi %d điểm đã cộng từ đơn hàng %s", 
            pointsToRefund, order.getCode()));
        refundHistory.setOrder(order);
        pointHistoryRepository.save(refundHistory);
        order.getPointHistories().add(refundHistory);

        log.debug("Đã thu hồi {} điểm đã cộng cho người dùng {} từ đơn hàng {}", 
            pointsToRefund, user.getEmail(), order.getCode());
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

}

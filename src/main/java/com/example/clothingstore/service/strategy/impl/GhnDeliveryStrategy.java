package com.example.clothingstore.service.strategy.impl;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.exception.DeliveryException;
import com.example.clothingstore.service.strategy.DeliveryStrategy;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import java.time.Instant;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GhnDeliveryStrategy implements DeliveryStrategy {

  private final Logger log = LoggerFactory.getLogger(GhnDeliveryStrategy.class);

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${ghn.api.url}")
  private String ghnApiUrl;

  @Value("${ghn.api.token}")
  private String ghnApiToken;

  @Value("${ghn.shop.id}")
  private String ghnShopId;

  @Value("${ghn.shop.district}")
  private Long shopDistrictId;

  @Value("${ghn.shop.weight}")
  private Long shopWeight;

  @Value("${ghn.shop.length}")
  private Long shopLength;

  @Value("${ghn.shop.width}")
  private Long shopWidth;

  @Value("${ghn.shop.height}")
  private Long shopHeight;

  @Value("${ghn.shop.free-shipping-threshold}")
  private Long shopFreeShippingThreshold;

  @Override
  public void processDelivery(Order order) {
    // Implementation for processing delivery
    return;
  }

  @Override
  public double calculateShippingFee(Order order) {
    try {
      // Get shipping information from order
      Long toDistrictId = order.getShippingInformation().getDistrictId();

      // Get service ID first
      String serviceId = getAvailableServiceId(toDistrictId);

      // Calculate shipping fee using the service ID
      return calculateFeeWithServiceId(serviceId, order);

    } catch (Exception e) {
      log.error("Error calculating GHN shipping fee: {}", e.getMessage());
      throw new DeliveryException(ErrorMessage.DELIVERY_CALCULATION_FAILED);
    }
  }

  @Override
  public double calculateShippingFee(double subtotal) {
    // Fallback simple calculation if no order details available
    if (subtotal < 1000000) {
      return 30000;
    }
    return 0;
  }

  private String getAvailableServiceId(Long toDistrictId) {
    String url = ghnApiUrl + "/shipping-order/available-services";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Token", ghnApiToken);

    JSONObject requestBody = new JSONObject();
    requestBody.put("shop_id", Long.parseLong(ghnShopId));
    requestBody.put("from_district", shopDistrictId);
    requestBody.put("to_district", toDistrictId);

    HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray data = jsonResponse.optJSONArray("data");

        if (data == null || data.length() == 0) {
          throw new DeliveryException(ErrorMessage.DELIVERY_AREA_NOT_SUPPORTED);
        }

        // Get first service ID (usually the fastest/default service)
        log.debug("Choose Service ID: {} for district ID: {}",
            data.getJSONObject(0).getInt("service_id"), toDistrictId);
        return String.valueOf(data.getJSONObject(0).getInt("service_id"));
      }
    } catch (Exception e) {
      log.error("Error getting GHN service ID: {}", e.getMessage());
      throw new DeliveryException(ErrorMessage.DELIVERY_SERVICE_UNAVAILABLE);
    }

    throw new DeliveryException(ErrorMessage.DELIVERY_SERVICE_UNAVAILABLE);
  }

  private double calculateFeeWithServiceId(String serviceId, Order order) {
    if (order.getTotal() - order.getDiscount() >= shopFreeShippingThreshold) {
      return 0;
    }

    String url = ghnApiUrl + "/shipping-order/fee";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Token", ghnApiToken);

    JSONObject requestBody = new JSONObject();
    requestBody.put("service_id", Long.parseLong(serviceId));
    requestBody.put("shop_id", Long.parseLong(ghnShopId));
    requestBody.put("to_district_id", order.getShippingInformation().getDistrictId());
    requestBody.put("to_ward_code", order.getShippingInformation().getWardId().toString());

    // Calculate total weight and dimensions of order
    requestBody.put("weight", shopWeight); // 5000g
    requestBody.put("length", shopLength); // 20cm
    requestBody.put("width", shopWidth); // 20cm
    requestBody.put("height", shopHeight); // 10cm

    // Set COD amount if payment method is COD
    if (order.getPaymentMethod() != null && order.getPaymentMethod() == PaymentMethod.COD) {
      requestBody.put("cod_amount", order.getTotal() - order.getDiscount());
    }

    HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

    try {
      log.debug("Request Body: {}", requestBody.toString());
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONObject data = jsonResponse.optJSONObject("data");

        if (data == null) {
          throw new DeliveryException(ErrorMessage.DELIVERY_FEE_CALCULATION_FAILED);
        }

        return data.getDouble("total");
      }
    } catch (Exception e) {
      log.error("Error calculating GHN shipping fee: {}", e.getMessage());
      throw new DeliveryException(ErrorMessage.DELIVERY_FEE_CALCULATION_FAILED);
    }

    throw new DeliveryException(ErrorMessage.DELIVERY_FEE_CALCULATION_FAILED);
  }

  @Override
  public Instant calculateEstimatedDeliveryDate(Order order) {
    String url = ghnApiUrl + "/shipping-order/leadtime";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Token", ghnApiToken);
    headers.set("ShopId", ghnShopId);

    // Get service ID for the delivery
    String serviceId = getAvailableServiceId(order.getShippingInformation().getDistrictId());

    JSONObject requestBody = new JSONObject();
    requestBody.put("from_district_id", shopDistrictId);
    requestBody.put("from_ward_code", ""); // Add your shop's ward code here
    requestBody.put("to_district_id", order.getShippingInformation().getDistrictId());
    requestBody.put("to_ward_code", order.getShippingInformation().getWardId().toString());
    requestBody.put("service_id", Integer.parseInt(serviceId));

    HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

    try {
      log.debug("Request Body for leadtime calculation: {}", requestBody.toString());
      ResponseEntity<String> response = 
          restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONObject data = jsonResponse.optJSONObject("data");

        if (data == null) {
          throw new DeliveryException(ErrorMessage.DELIVERY_CALCULATION_FAILED);
        }

        // GHN returns leadtime as Unix timestamp in seconds
        long leadtime = data.getLong("leadtime");
        return Instant.ofEpochSecond(leadtime);
      }
    } catch (Exception e) {
      log.error("Error calculating GHN estimated delivery date: {}", e.getMessage());
      throw new DeliveryException(ErrorMessage.DELIVERY_CALCULATION_FAILED);
    }

    throw new DeliveryException(ErrorMessage.DELIVERY_CALCULATION_FAILED);
  }
}

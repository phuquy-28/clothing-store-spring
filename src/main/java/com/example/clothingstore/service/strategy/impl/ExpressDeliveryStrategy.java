package com.example.clothingstore.service.strategy.impl;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.service.strategy.DeliveryStrategy;
import com.example.clothingstore.client.GoongClient;
import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.response.DistanceMatrixResponse;
import com.example.clothingstore.dto.response.GeocodingResponse;
import com.example.clothingstore.exception.DeliveryException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExpressDeliveryStrategy implements DeliveryStrategy {
  private final GoongClient goongClient;

  @Value("${goong.api.key}")
  private String apiKey;

  @Value("${goong.api.vehicle-type}")
  private String vehicleType;

  @Value("${ghn.shop.free-shipping-threshold}")
  private Long freeShippingThreshold;

  public ExpressDeliveryStrategy(GoongClient goongClient) {
    this.goongClient = goongClient;
  }

  @Override
  public void processDelivery(Order order) {
    return;
  }

  @Override
  public double calculateShippingFee(Order order) {
    if (order == null || order.getShippingInformation() == null) {
      return 0;
    }

    // Validate delivery area (HCMC only)
    validateDeliveryArea(order.getShippingInformation());

    double orderTotal = order.getTotal() != null ? order.getTotal() : 0;
    double discount = order.getDiscount() != null ? order.getDiscount() : 0;
    double pointDiscount = order.getPointDiscount() != null ? order.getPointDiscount() : 0;

    // Check if order qualifies for free shipping
    if (orderTotal - discount - pointDiscount >= freeShippingThreshold) {
      return 0;
    }

    // Calculate distance-based fee for express delivery
    String fullAddress = buildFullAddress(order.getShippingInformation());
    double distanceInKm = calculateDistanceToCustomer(fullAddress);

    return calculateExpressFeeByDistance(distanceInKm);
  }

  private void validateDeliveryArea(Order.ShippingInformation shippingInfo) {
    if (shippingInfo.getProvince() == null
        || !AppConstant.HCMC_PROVINCE_ID.equals(shippingInfo.getProvinceId())) {
      log.info("Delivery area not supported: {} {}", shippingInfo.getProvince(),
          shippingInfo.getProvinceId());
      throw new DeliveryException(ErrorMessage.DELIVERY_AREA_NOT_SUPPORTED);
    }
  }

  private String buildFullAddress(Order.ShippingInformation shippingInfo) {
    StringBuilder addressBuilder = new StringBuilder();

    // Add address details in order: specific address, ward, district, province
    if (shippingInfo.getAddress() != null) {
      addressBuilder.append(shippingInfo.getAddress().trim());
    }

    if (shippingInfo.getWard() != null) {
      if (addressBuilder.length() > 0)
        addressBuilder.append(", ");
      addressBuilder.append(shippingInfo.getWard().trim());
    }

    if (shippingInfo.getDistrict() != null) {
      if (addressBuilder.length() > 0)
        addressBuilder.append(", ");
      addressBuilder.append(shippingInfo.getDistrict().trim());
    }

    if (shippingInfo.getProvince() != null) {
      if (addressBuilder.length() > 0)
        addressBuilder.append(", ");
      addressBuilder.append(shippingInfo.getProvince().trim());
    }

    // Add country to improve geocoding accuracy
    addressBuilder.append(", Việt Nam");

    return addressBuilder.toString();
  }

  private double calculateDistanceToCustomer(String customerAddress) {
    try {
      // Convert customer address to coordinates
      GeocodingResponse.GeocodingResult.Geometry.Location customerLocation =
          geocodeAddress(customerAddress);

      // Calculate distance
      return calculateDistance(AppConstant.STORE_LATITUDE, AppConstant.STORE_LONGITUDE,
          customerLocation.getLat(), customerLocation.getLng());
    } catch (Exception e) {
      log.info("Error calculating distance to customer address: {}", e.getMessage());
      throw new DeliveryException(ErrorMessage.DELIVERY_FEE_CALCULATION_FAILED);
    }
  }

  private GeocodingResponse.GeocodingResult.Geometry.Location geocodeAddress(String address) {
    log.debug("Geocoding address: {}", address);
    GeocodingResponse response = goongClient.geocodeAddress(address, apiKey);
    log.debug("Geocoding response: {}", response);

    if (response.getResults() == null || response.getResults().isEmpty()) {
      log.info("Could not geocode address: {}", address);
      throw new DeliveryException(ErrorMessage.DELIVERY_FEE_CALCULATION_FAILED);
    }

    // Get the first (most relevant) result
    GeocodingResponse.GeocodingResult result = response.getResults().get(0);

    // Validate that the geocoded address is in HCMC
    validateGeocodeResult(result, address);

    return result.getGeometry().getLocation();
  }

  private void validateGeocodeResult(GeocodingResponse.GeocodingResult result,
      String originalAddress) {
    String formattedAddress = result.getFormatted_address();

    // Check if the formatted address contains "Hồ Chí Minh" or "TP.HCM"
    if (formattedAddress == null
        || (!formattedAddress.contains("Hồ Chí Minh") && !formattedAddress.contains("TP.HCM")
            && !formattedAddress.contains("Thành phố Hồ Chí Minh"))) {
      log.info("Address not in HCMC: {} (geocoded as: {})", originalAddress, formattedAddress);
      throw new DeliveryException(ErrorMessage.DELIVERY_AREA_NOT_SUPPORTED);
    }
  }

  private double calculateDistance(double fromLat, double fromLng, double toLat, double toLng) {
    String origins = fromLat + "," + fromLng;
    String destinations = toLat + "," + toLng;

    try {
      log.debug("Calculating distance between {} and {}", origins, destinations);
      DistanceMatrixResponse response =
          goongClient.calculateDistance(origins, destinations, vehicleType, apiKey);
      log.debug("Distance matrix response: {}", response);

      if (response.getRows() == null || response.getRows().isEmpty()
          || response.getRows().get(0).getElements() == null
          || response.getRows().get(0).getElements().isEmpty()) {
        throw new DeliveryException(ErrorMessage.DELIVERY_FEE_CALCULATION_FAILED);
      }

      DistanceMatrixResponse.Row.Element element = response.getRows().get(0).getElements().get(0);
      if (!"OK".equals(element.getStatus())) {
        throw new DeliveryException(ErrorMessage.DELIVERY_FEE_CALCULATION_FAILED);
      }

      // Convert meters to kilometers
      return element.getDistance().getValue() / 1000.0;
    } catch (Exception e) {
      log.info("Error calculating distance: {}", e.getMessage());
      throw new DeliveryException(ErrorMessage.DELIVERY_FEE_CALCULATION_FAILED);
    }
  }

  private double calculateExpressFeeByDistance(double distanceInKm) {
    // Base fee for first 2km
    double totalFee = AppConstant.EXPRESS_BASE_FEE;
    
    // If distance is greater than base distance, add per-km fee
    if (distanceInKm > AppConstant.EXPRESS_BASE_DISTANCE) {
      double additionalDistance = distanceInKm - AppConstant.EXPRESS_BASE_DISTANCE;
      totalFee += additionalDistance * AppConstant.EXPRESS_PER_KM_FEE;
    }
    
    return totalFee;
  }

  @Override
  public Instant calculateEstimatedDeliveryDate(Order order) {
    return Instant.now().plusSeconds(7200); // 2 hours from now
  }
}

package com.example.clothingstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.annotation.ApiMessage;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class PaymentController {

  private final Logger log = LoggerFactory.getLogger(PaymentController.class);

  private final VnPayService vnPayService;

  @ApiMessage("Payment success")
  @GetMapping(UrlConfig.PAYMENT + UrlConfig.VNPAY_RETURN)
  public ResponseEntity<Void> validatePayment(HttpServletRequest request) {
    log.debug("REST request to validate payment with order info: {}",
        request.getParameter("vnp_OrderInfo"));
    vnPayService.validatePayment(request);
    return ResponseEntity.ok().build();
  }

}

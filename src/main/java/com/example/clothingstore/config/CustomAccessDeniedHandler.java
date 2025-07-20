package com.example.clothingstore.config;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.response.RestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  private final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

  public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    log.info("Access denied: {}", accessDeniedException.getMessage());
    RestResponse<Object> restResponse = new RestResponse<>();
    restResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
    restResponse.setError("Access Denied");
    restResponse.setMessage(Translator.toLocale(ErrorMessage.ACCESS_DENIED));

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(restResponse));
  }
}

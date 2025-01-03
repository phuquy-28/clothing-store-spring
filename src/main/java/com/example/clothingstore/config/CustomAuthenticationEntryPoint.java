package com.example.clothingstore.config;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.response.RestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

  private final ObjectMapper mapper;

  public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    this.delegate.commence(request, response, authException);
    response.setContentType("application/json;charset=UTF-8");

    RestResponse<Object> res = new RestResponse<Object>();
    res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

    String errorMessage = Optional.ofNullable(authException.getCause()).map(Throwable::getMessage)
        .orElse(authException.getMessage());
    res.setError(errorMessage);

    res.setMessage(Translator.toLocale(ErrorMessage.ACCESS_TOKEN_INVALID));

    mapper.writeValue(response.getWriter(), res);
  }
}
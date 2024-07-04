package com.example.clothingstore.utils;

import com.example.clothingstore.domain.dto.response.RestResponse;
import com.example.clothingstore.utils.annotation.ApiMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


@ControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(MethodParameter returnType, Class converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType,
      MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request,
      ServerHttpResponse response) {
    response = ServletServerHttpResponse.class.cast(response);
    HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
    int status = servletResponse.getStatus();
    RestResponse<Object> restResponse = new RestResponse<>();

    if (body instanceof String || body instanceof Resource) {
      return body;
    }

    String path = request.getURI().getPath();
    if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
      return body;
    }

    // error case
    if (status >= 400) {
      return body;
    } else { // success case
      restResponse.setStatusCode(status);
      ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
      restResponse.setMessage(apiMessage == null ? "CALL API SUCCESS" : apiMessage.value());
      restResponse.setData(body);
    }
    return restResponse;
  }
}

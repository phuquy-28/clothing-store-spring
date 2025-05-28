package com.example.clothingstore.exception;

import com.example.clothingstore.config.Translator;
import com.example.clothingstore.dto.response.RestResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalException {

  // @ExceptionHandler(value = Exception.class)
  @ExceptionHandler(value = AccessDeniedException.class)
  public ResponseEntity<RestResponse<Object>> handleAllException(Exception ex) {
    if (ex instanceof AccessDeniedException) {
      throw (AccessDeniedException) ex; // Re-throw AccessDeniedException to be handled by
                                        // CustomAccessDeniedHandler
    }
    RestResponse<Object> res = new RestResponse<Object>();
    res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    res.setMessage(ex.getMessage());
    res.setError("Internal Server Error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
  }

  @ExceptionHandler(value = {UsernameNotFoundException.class, IdInvalidException.class,
      EmailInvalidException.class, TokenInvalidException.class, MissingRequestCookieException.class,
      ResourceNotFoundException.class, ResourceAlreadyExistException.class,
      InvalidFileTypeException.class, BadRequestException.class, BadCredentialsException.class,
      PaymentException.class, OrderCreationException.class, DeliveryException.class})
  public ResponseEntity<RestResponse<Object>> handleIdException(Exception idException) {
    RestResponse<Object> res = new RestResponse<Object>();
    res.setStatusCode(HttpStatus.BAD_REQUEST.value());
    res.setError("Exception occurs...");
    res.setMessage(Translator.toLocale(idException.getMessage()));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
  }

  // NoResourceFoundException
  @ExceptionHandler(value = NoResourceFoundException.class)
  public ResponseEntity<RestResponse<Object>> handleNoResourceFoundException(
      NoResourceFoundException e) {
    RestResponse<Object> responseEntity = new RestResponse<Object>();
    responseEntity.setStatusCode(HttpStatus.NOT_FOUND.value());
    responseEntity.setError("Resource not found");
    responseEntity.setMessage(e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseEntity);
  }

  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException e) {
    List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

    List<String> errors = fieldErrors.stream().map(fieldError -> {
      return Translator.toLocale(fieldError.getDefaultMessage());
    }).collect(Collectors.toList());

    RestResponse<Object> responseEntity = new RestResponse<>();
    responseEntity.setStatusCode(HttpStatus.BAD_REQUEST.value());
    responseEntity.setError("Invalid request content.");
    responseEntity.setMessage(errors.size() > 1 ? errors : errors.get(0));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEntity);
  }

  @ExceptionHandler(com.example.clothingstore.exception.AccessDeniedException.class)
  public ResponseEntity<RestResponse<Object>> handleAccessDeniedException(
      com.example.clothingstore.exception.AccessDeniedException e) {
    RestResponse<Object> res = new RestResponse<>();
    res.setStatusCode(HttpStatus.FORBIDDEN.value());
    res.setError("Access denied");
    res.setMessage(Translator.toLocale(e.getMessage()));
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
  }

  @ExceptionHandler(value = DataValidationException.class)
  public ResponseEntity<RestResponse<Object>> handleDataValidationException(
      DataValidationException ex) {
    RestResponse<Object> res = new RestResponse<>();
    res.setStatusCode(HttpStatus.BAD_REQUEST.value());
    res.setError("Data Validation Error");
    res.setMessage(Translator.toLocale(ex.getMessageKey(),
        ex.getArgs() != null ? ex.getArgs() : new Object[] {}));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<RestResponse<Object>> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex) {
    RestResponse<Object> res = new RestResponse<>();
    res.setStatusCode(HttpStatus.BAD_REQUEST.value());
    res.setError("File Too Large");
    res.setMessage(Translator.toLocale("import.error.file_too_large_configured"));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
  }

}

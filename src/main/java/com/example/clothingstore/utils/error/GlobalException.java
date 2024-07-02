package com.example.clothingstore.utils.error;

import com.example.clothingstore.domain.dto.response.RestResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            IdInvalidException.class,
            MissingRequestCookieException.class,
//            Exception.class
    })
    public ResponseEntity<RestResponse<Object>> handleIdException(Exception idException) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(idException.getMessage());
        res.setMessage("Exception occurs...");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    //NoResourceFoundException
    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleNoResourceFoundException(NoResourceFoundException e) {
        RestResponse<Object> responseEntity = new RestResponse<Object>();
        responseEntity.setStatusCode(HttpStatus.NOT_FOUND.value());
        responseEntity.setError(e.getMessage());
        responseEntity.setMessage("Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseEntity);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        RestResponse<Object> responseEntity = new RestResponse<Object>();
        responseEntity.setStatusCode(HttpStatus.BAD_REQUEST.value());
        responseEntity.setError(e.getBody().getDetail());

        List<String> errors = fieldErrors.stream().map(fieldError -> fieldError.getDefaultMessage()).toList();
        responseEntity.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEntity);
    }

}

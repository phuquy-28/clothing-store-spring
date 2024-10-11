package com.example.clothingstore.exception;

public class AccessDeniedException extends RuntimeException {

  public AccessDeniedException(String message) {
    super(message);
  }
}

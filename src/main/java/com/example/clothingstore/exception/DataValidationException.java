package com.example.clothingstore.exception;

import lombok.Getter;

@Getter
public class DataValidationException extends RuntimeException {
  private final String messageKey;
  private final Object[] args;

  public DataValidationException(String messageKey, Object... args) {
    super(messageKey);
    this.messageKey = messageKey;
    this.args = args;
  }

  public DataValidationException(String messageKey) {
    super(messageKey);
    this.messageKey = messageKey;
    this.args = null;
  }
}

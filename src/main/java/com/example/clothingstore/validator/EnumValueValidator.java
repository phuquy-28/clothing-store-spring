package com.example.clothingstore.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.example.clothingstore.annotation.EnumValue;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {
  private List<String> acceptedValues;

  @Override
  public void initialize(EnumValue annotation) {
    acceptedValues = Stream.of(annotation.enumClass().getEnumConstants()).map(Enum::name)
        .collect(Collectors.toList());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return acceptedValues.contains(value.toUpperCase());
  }
}

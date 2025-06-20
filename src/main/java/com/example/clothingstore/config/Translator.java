package com.example.clothingstore.config;

import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class Translator {

  private static ResourceBundleMessageSource messageSource;

  public Translator(ResourceBundleMessageSource messageSource) {
    Translator.messageSource = messageSource;
  }

  public static String toLocale(String msgCode, Object... args) {
    // Locale locale = LocaleContextHolder.getLocale();
    Locale locale = new Locale("vi");
    return messageSource.getMessage(msgCode, args, locale);
  }

}

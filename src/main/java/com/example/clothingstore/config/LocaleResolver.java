package com.example.clothingstore.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class LocaleResolver extends AcceptHeaderLocaleResolver implements WebMvcConfigurer {

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    String language = request.getHeader("Accept-Language");
    if (language == null || language.isEmpty()) {
      return new Locale("vi"); // Default to Vietnamese if no language is specified
    }

    List<Locale> supportedLocales = List.of(new Locale("en"), new Locale("vi"));
    Locale resolvedLocale = Locale.lookup(Locale.LanguageRange.parse(language), supportedLocales);

    return resolvedLocale != null ? resolvedLocale : new Locale("vi");
  }

  @Bean
  public ResourceBundleMessageSource messageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setBasename("i18n/messages");
    source.setDefaultEncoding("UTF-8");
    source.setCacheSeconds(3600);
    source.setUseCodeAsDefaultMessage(true);
    return source;
  }

  @Bean
  public LocalValidatorFactoryBean getValidator() {
    LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(messageSource());
    return bean;
  }

}

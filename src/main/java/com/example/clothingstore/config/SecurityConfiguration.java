package com.example.clothingstore.config;

import static com.example.clothingstore.util.SecurityUtil.JWT_ALGORITHM;
import static com.example.clothingstore.constant.UrlConfig.*;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import java.util.Collections;
import com.example.clothingstore.repository.TokenBlacklistRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@Slf4j
@RequiredArgsConstructor
public class SecurityConfiguration {

  @Value("${jwt.base64-secret}")
  private String jwtKey;

  private final TokenBlacklistRepository tokenBlacklistRepository;

  private SecretKey getSecretKey() {
    byte[] keyBytes = Base64.from(jwtKey).decode();
    return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
      CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
    http.csrf(c -> c.disable()).cors(Customizer.withDefaults())
        .authorizeHttpRequests(authz -> authz.requestMatchers(PUBLIC_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.PUT, PUBLIC_PUT_ENDPOINTS()).permitAll().anyRequest()
            .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
            .authenticationEntryPoint(customAuthenticationEntryPoint)
            .accessDeniedHandler(customAccessDeniedHandler))
        .formLogin(f -> f.disable()).sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthorityPrefix("");
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder =
        NimbusJwtDecoder.withSecretKey(getSecretKey()).macAlgorithm(JWT_ALGORITHM).build();

    jwtDecoder.setJwtValidator(token -> {
      // Get the current request from RequestContextHolder
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Check if the request is a public endpoint based on HTTP method
        if (isPublicEndpoint(path, method)) {
          return OAuth2TokenValidatorResult.success();
        }
      }

      // Check if token is blacklisted
      if (tokenBlacklistRepository.findByToken(token.getTokenValue()).isPresent()) {
        throw new JwtValidationException(null,
            Collections.singletonList(new OAuth2Error("invalid_token")));
      }

      // Validate expiry
      return new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(),
          new JwtTimestampValidator(Duration.ofSeconds(0))).validate(token);
    });

    return jwtDecoder;
  }

  private boolean isPublicEndpoint(String path, String method) {
    switch (method.toUpperCase()) {
      case "GET":
        return Arrays.stream(PUBLIC_GET_ENDPOINTS()).anyMatch(path::startsWith);
      case "POST":
        return Arrays.stream(PUBLIC_POST_ENDPOINTS()).anyMatch(path::startsWith);
      case "PUT":
        return Arrays.stream(PUBLIC_PUT_ENDPOINTS()).anyMatch(path::startsWith);
      default:
        return Arrays.stream(PUBLIC_ENDPOINTS()).anyMatch(path::startsWith);
    }
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}

package com.example.clothingstore.config;

import static com.example.clothingstore.util.SecurityUtil.JWT_ALGORITHM;
import static com.example.clothingstore.constant.UrlConfig.*;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import java.time.Duration;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.core.OAuth2Error;
import java.util.Collections;
import com.example.clothingstore.repository.TokenBlacklistRepository;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@Slf4j
@RequiredArgsConstructor
public class SecurityConfiguration {

  @Value("${jwt.base64-secret}")
  private String jwtKey;

  private final TokenBlacklistRepository tokenBlacklistRepository;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = Base64.from(jwtKey).decode();
    return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
  }

  @Bean
  @Order(1)
  public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
    String[] allPublicPaths = combineArrays(PUBLIC_ENDPOINTS(), PUBLIC_WS_ENDPOINTS(),
        PUBLIC_GET_ENDPOINTS(), PUBLIC_POST_ENDPOINTS(), PUBLIC_PUT_ENDPOINTS());

    http.securityMatcher(allPublicPaths).csrf(c -> c.disable()).cors(Customizer.withDefaults())
        .authorizeHttpRequests(authz -> authz.requestMatchers(PUBLIC_ENDPOINTS()).permitAll()
            .requestMatchers(PUBLIC_WS_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.PUT, PUBLIC_PUT_ENDPOINTS()).permitAll().anyRequest()
            .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  private String[] combineArrays(String[]... arrays) {
    return Arrays.stream(arrays).flatMap(Arrays::stream).distinct().toArray(String[]::new);
  }

  @Bean
  @Order(2)
  public SecurityFilterChain privateSecurityFilterChain(HttpSecurity http,
      CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
      CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
    http.securityMatcher("/**").csrf(c -> c.disable()).cors(Customizer.withDefaults())
        .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
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

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}

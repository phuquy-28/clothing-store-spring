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
import org.springframework.security.oauth2.jwt.JwtValidationException;
import java.util.Collections;
import com.example.clothingstore.repository.TokenBlacklistRepository;
import org.springframework.web.context.request.RequestContextHolder;

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
    http.csrf(c -> c.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(PUBLIC_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS()).permitAll()
            .requestMatchers(HttpMethod.PUT, PUBLIC_PUT_ENDPOINTS()).permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .decoder(jwtDecoder())
                .authenticationManager(token -> {
                    try {
                        return token;
                    } catch (Exception e) {
                        String requestPath = ((HttpServletRequest) RequestContextHolder
                            .currentRequestAttributes())
                            .getRequestURI();
                        
                        // Check if the current path is public
                        if (isPublicPath(requestPath)) {
                            return token;
                        }
                        throw e;
                    }
                })
            )
            .authenticationEntryPoint(customAuthenticationEntryPoint)
            .accessDeniedHandler(customAccessDeniedHandler)
        )
        .formLogin(f -> f.disable())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

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

  private boolean isPublicPath(String path) {
    // Check exact matches
    for (String endpoint : PUBLIC_ENDPOINTS()) {
        if (path.equals(endpoint)) return true;
    }
    
    // Check GET endpoints
    for (String endpoint : PUBLIC_GET_ENDPOINTS()) {
        if (pathMatches(path, endpoint)) return true;
    }
    
    // Check POST endpoints
    for (String endpoint : PUBLIC_POST_ENDPOINTS()) {
        if (pathMatches(path, endpoint)) return true;
    }
    
    // Check PUT endpoints
    for (String endpoint : PUBLIC_PUT_ENDPOINTS()) {
        if (pathMatches(path, endpoint)) return true;
    }
    
    return false;
  }

  private boolean pathMatches(String path, String pattern) {
    // Escape special regex characters and convert Spring URL patterns to regex patterns
    String regexPattern = pattern
        // Escape special regex characters
        .replace(".", "\\.")
        // Convert /** wildcard
        .replace("/**", ".*")
        // Convert /* wildcard
        .replace("/*", "/[^/]*")
        // Convert URL template variables (e.g., {slug}, {id})
        .replaceAll("\\{[^/]+\\}", "[^/]+");

    return path.matches(regexPattern);
  }
}

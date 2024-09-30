package com.example.clothingstore.util;


import com.example.clothingstore.constant.AuthoritiesConstant;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.LoginResDTO.UserInsideToken;
import com.example.clothingstore.entity.User;
import com.nimbusds.jose.util.Base64;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;


@Service
public class SecurityUtil {

  public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

  @Value("${jwt.base64-secret}")
  private String jwtKey;

  @Value("${jwt.access-token-validity-in-seconds}")
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token-validity-in-seconds}")
  private long refreshTokenExpiration;

  private final JwtEncoder jwtEncoder;

  public SecurityUtil(JwtEncoder jwtEncoder) {
    this.jwtEncoder = jwtEncoder;
  }

  public String createAccessToken(User user, LoginResDTO loginResDTO) {
    LoginResDTO.UserInsideToken userInsideToken = convertLoginResDTOToUserInsideToken(loginResDTO);
    Instant now = Instant.now();
    Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuedAt(now)
        .expiresAt(validity)
        .subject(loginResDTO.getUser().getEmail())
        .claim("scope", buildScope(user))
        .claim("user", userInsideToken)
        .build();
    JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
    return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
        claims)).getTokenValue();
  }

  public String createRefreshToken(String email, LoginResDTO loginResDTO) {
    LoginResDTO.UserInsideToken userInsideToken = convertLoginResDTOToUserInsideToken(loginResDTO);
    Instant now = Instant.now();
    Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuedAt(now)
        .expiresAt(validity)
        .subject(email)
        .claim("user", userInsideToken)
        .build();
    JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
    return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
        claims)).getTokenValue();
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = Base64.from(jwtKey).decode();
    return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
  }

  public Jwt jwtDecoder(String token) {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
        getSecretKey()).macAlgorithm(JWT_ALGORITHM).build();
    // Set a clock skew to handle token expiration window
    jwtDecoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<Jwt>(JwtValidators.createDefault(),
            new JwtTimestampValidator(Duration.ofSeconds(0))));
    try {
      return jwtDecoder.decode(token);
    } catch (Exception e) {
      System.out.println(">>> JWT error: " + e.getMessage());
      throw e;
    }
  }

  public static Optional<String> getCurrentUserLogin() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
  }

  private static String extractPrincipal(Authentication authentication) {
    if (authentication == null) {
      return null;
    } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
      return springSecurityUser.getUsername();
    } else if (authentication.getPrincipal() instanceof Jwt jwt) {
      return jwt.getSubject();
    } else if (authentication.getPrincipal() instanceof String s) {
      return s;
    }
    return null;
  }

  public static Optional<String> getCurrentUserJWT() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(securityContext.getAuthentication())
        .filter(authentication -> authentication.getCredentials() instanceof String)
        .map(authentication -> (String) authentication.getCredentials());
  }

  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && getAuthorities(authentication).noneMatch(
        AuthoritiesConstant.ANONYMOUS::equals);
  }

  public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (
        authentication != null && getAuthorities(authentication).anyMatch(
            authority -> Arrays.asList(authorities).contains(authority))
    );
  }

  public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
    return !hasCurrentUserAnyOfAuthorities(authorities);
  }

  public static boolean hasCurrentUserThisAuthority(String authority) {
    return hasCurrentUserAnyOfAuthorities(authority);
  }

  private static Stream<String> getAuthorities(Authentication authentication) {
    return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
  }

  private String buildScope(User user) {
    StringBuilder scope = new StringBuilder();
    if (user.getRole() != null) {
      scope.append("ROLE_").append(user.getRole().getName());
    }
    return scope.toString();
  }

  public UserInsideToken convertLoginResDTOToUserInsideToken(LoginResDTO loginResDTO) {
    UserInsideToken userInsideToken = new UserInsideToken();
    userInsideToken.setId(loginResDTO.getUser().getId());
    userInsideToken.setEmail(loginResDTO.getUser().getEmail());
    userInsideToken.setFirstName(loginResDTO.getUser().getFirstName());
    userInsideToken.setLastName(loginResDTO.getUser().getLastName());
    return userInsideToken;
  }
}

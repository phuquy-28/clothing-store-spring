package com.example.clothingstore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

  private final JwtDecoder jwtDecoder;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      // Extract authorization header
      List<String> authorization = accessor.getNativeHeader("Authorization");
      log.debug("WebSocket Connection attempt with Authorization: {}", authorization);

      if (authorization != null && !authorization.isEmpty()) {
        String bearerToken = authorization.get(0);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
          String token = bearerToken.substring(7);
          try {
            // Validate JWT token
            Jwt jwt = jwtDecoder.decode(token);

            // Extract user information from JWT
            String username = jwt.getSubject();
            List<String> authorities = jwt.getClaimAsStringList("authorities");

            // Create authentication object
            List<SimpleGrantedAuthority> grantedAuthorities = authorities != null
                ? authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                : List.of();

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);

            // Set authentication in the accessor
            accessor.setUser(auth);
            log.debug("WebSocket Connection authenticated for user: {}", username);
          } catch (JwtException e) {
            log.error("Invalid JWT token in WebSocket connection: {}", e.getMessage());
          }
        }
      } else {
        log.debug("WebSocket Connection attempt without Authorization header");
      }
    }

    return message;
  }
}

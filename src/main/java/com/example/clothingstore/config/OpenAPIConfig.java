package com.example.clothingstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
public class OpenAPIConfig {

  @Value("${domain}")
  private String domain;

  private SecurityScheme createAPIKeyScheme() {
    return new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer");
  }

  private Server createServer(String url, String description) {
    Server server = new Server();
    server.setUrl(url);
    server.setDescription(description);
    return server;
  }

  private Info createApiInfo() {
    return new Info().title("Ecommerce Fashion API").version("1.0")
        .description("This API exposes all endpoints for the Ecommerce Fashion application");
  }

  @Bean
  public OpenAPI myOpenAPI() {
    return new OpenAPI()
        .info(createApiInfo())
        .servers(List.of(
            createServer(domain, "Server URL")))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .schemaRequirement("Bearer Authentication", createAPIKeyScheme());
  }
}

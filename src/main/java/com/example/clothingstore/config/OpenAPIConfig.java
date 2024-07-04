package com.example.clothingstore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

  private SecurityScheme createAPIKeyScheme() {
    return new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .bearerFormat("JWT")
        .scheme("bearer");
  }

  private Server createServer(String url, String description) {
    Server server = new Server();
    server.setUrl(url);
    server.setDescription(description);
    return server;
  }

  private Info createApiInfo() {
    return new Info()
        .title("Minimog Store API")
        .version("1.0")
        .description("This API exposes all endpoints (clothing store)");
  }

  @Bean
  public OpenAPI myOpenAPI() {
    return new OpenAPI()
        .info(createApiInfo())
        .servers(List.of(
            createServer("http://localhost:8080", "Server URL in Development environment"),
            createServer("https://localhost:8080", "Server URL in Production environment")))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(
            new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
  }
}

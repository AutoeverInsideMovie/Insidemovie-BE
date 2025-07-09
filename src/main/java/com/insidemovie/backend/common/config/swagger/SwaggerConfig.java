package com.insidemovie.backend.common.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("/");

        return new OpenAPI()
                .info(new Info()
                        .title("Inside Movie")
                        .description("Inside Movie REST API Document")
                        .version("1.0.0"))
                .addServersItem(server);
    }
}

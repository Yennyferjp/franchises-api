
package com.yennyfer.franchises_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franchisesApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Franchises API")
                        .description("API para la gestión de franquicias, sucursales y productos")
                        .version("1.0.0"));
    }
}

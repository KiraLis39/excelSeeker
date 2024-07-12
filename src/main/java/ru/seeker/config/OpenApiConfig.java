package ru.seeker.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("EXCEL-SEEKER-SERVICE")
                        .description("Сервис обработки и хранения данных")
                        .version("API 0.1.6") // as application:version
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Табличные данные поставщиков")
                        .url("https://none/pages/viewpage.action?pageId=001"));
    }
}

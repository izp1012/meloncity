package com.meloncity.citiz.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Melon API")
                        .version("1.0")
                        .description("")
                        .contact(new Contact()
                                .name("개발팀")
                                .email("izp1012@naver.com"))
                )

                // 추가 설정 속성
                // API가 배포된 서버들을 정의
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Server")  // 로컬 서버 설정
                ));
    }
}
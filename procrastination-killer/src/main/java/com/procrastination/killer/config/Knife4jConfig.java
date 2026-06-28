package com.procrastination.killer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / Swagger 接口文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 全局 JWT 认证头
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Bearer 认证")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("在下方输入 Bearer {token} 进行认证");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer 认证");

        return new OpenAPI()
                .info(new Info()
                        .title("拖延症杀手 API 文档")
                        .version("1.0.0")
                        .description("AI-Powered Procrastination Killer - 帮助用户将宏观目标拆解为可执行的微小任务")
                        .contact(new Contact()
                                .name("Procrastination Killer Team"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(securityRequirement)
                .schemaRequirement("Bearer 认证", securityScheme);
    }
}

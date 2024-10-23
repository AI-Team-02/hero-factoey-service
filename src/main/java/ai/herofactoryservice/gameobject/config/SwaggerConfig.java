package ai.herofactoryservice.gameobject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hero Factory Game Object API")
                        .description("Game Object Generation API with FastAPI Integration")
                        .version("1.0"));
    }
}
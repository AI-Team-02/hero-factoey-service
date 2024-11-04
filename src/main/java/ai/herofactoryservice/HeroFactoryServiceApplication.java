package ai.herofactoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HeroFactoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeroFactoryServiceApplication.class, args);
    }

}

package com.gameservice.create_game_resource_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication
@EnableSwagger2
public class CreateGameResourceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreateGameResourceServiceApplication.class, args);
	}

}

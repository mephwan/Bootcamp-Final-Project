package com.bootcamp.PriceAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PriceAPIApplication {

	public static void main(String[] args) {
		SpringApplication.run(PriceAPIApplication.class, args);
	}

}

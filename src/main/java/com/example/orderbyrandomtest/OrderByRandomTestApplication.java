package com.example.orderbyrandomtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OrderByRandomTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderByRandomTestApplication.class, args);
	}

}

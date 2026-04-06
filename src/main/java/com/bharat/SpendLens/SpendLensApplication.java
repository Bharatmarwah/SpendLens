package com.bharat.SpendLens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpendLensApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpendLensApplication.class, args);
	}

}

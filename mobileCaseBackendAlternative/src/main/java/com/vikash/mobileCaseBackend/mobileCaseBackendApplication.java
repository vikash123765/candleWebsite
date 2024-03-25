package com.vikash.mobileCaseBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Enable Spring's scheduled task execution
public class mobileCaseBackendApplication {


	public static void main(String[] args) {
		SpringApplication.run(mobileCaseBackendApplication.class, args);
	}

}

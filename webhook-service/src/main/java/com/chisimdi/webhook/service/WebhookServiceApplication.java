package com.chisimdi.webhook.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WebhookServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebhookServiceApplication.class, args);
	}

}

package com.example.Telegram.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramBootApplication.class, args);
		System.out.println("Hello");
	}

}

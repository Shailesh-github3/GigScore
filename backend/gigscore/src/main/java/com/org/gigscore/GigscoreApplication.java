package com.org.gigscore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class GigscoreApplication {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${gemini.api.key}")
	private String geminiApiKey;

	public static void main(String[] args) {
		SpringApplication.run(GigscoreApplication.class, args);
	}

	@PostConstruct
	public void validateEnvironmentVariables() {
		if (jwtSecret == null || jwtSecret.isBlank()) {
			throw new IllegalStateException("JWT_SECRET environment variable is not set. Please configure it before starting the application.");
		}
		if (geminiApiKey == null || geminiApiKey.isBlank()) {
			throw new IllegalStateException("GEMINI_API_KEY environment variable is not set. Please configure it before starting the application.");
		}
	}

}

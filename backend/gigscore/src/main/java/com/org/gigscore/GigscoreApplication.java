package com.org.gigscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GigscoreApplication {
	public static void main(String[] args) {
		SpringApplication.run(GigscoreApplication.class, args);
	}

}

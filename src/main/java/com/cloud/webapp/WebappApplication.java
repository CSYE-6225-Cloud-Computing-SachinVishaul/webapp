package com.cloud.webapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class WebappApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}
	
	@Override
    public void run(String... args) throws Exception {
        // Load .env file
        Dotenv dotenv = Dotenv.load();

    }
}

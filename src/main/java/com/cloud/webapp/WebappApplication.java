package com.cloud.webapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class WebappApplication implements CommandLineRunner{

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        // Set system properties so that Spring Boot can resolve placeholders
        System.setProperty("S3_BUCKET_NAME", dotenv.get("S3_BUCKET_NAME"));
        System.setProperty("AWS_REGION", dotenv.get("AWS_REGION"));
		SpringApplication.run(WebappApplication.class, args);
	}
	
	@Override
    public void run(String... args) throws Exception {
        // Load .env file
        // Dotenv dotenv = Dotenv.load();
//        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
//        String bucketName = dotenv.get("S3_BUCKET_NAME"); 
//        String awsRegion = dotenv.get("AWS_REGION");
//        // then store or set them as system properties if needed
//        System.setProperty("S3_BUCKET_NAME", bucketName);
//        System.setProperty("AWS_REGION", awsRegion);

    }
}

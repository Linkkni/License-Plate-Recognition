package com.carlisenceplate.lisenceplate;

import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LisenceplateApplication {

	public static void main(String[] args) {
		OpenCV.loadLocally();

		SpringApplication.run(LisenceplateApplication.class, args);
	}

}

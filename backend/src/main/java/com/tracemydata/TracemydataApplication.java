package com.tracemydata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.tracemydata")
public class TracemydataApplication {

	public static void main(String[] args) {
		SpringApplication.run(TracemydataApplication.class, args);
	}

}

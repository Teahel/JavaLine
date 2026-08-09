package com.example.bd_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//mvn clean package -P cloud -DskipTests=true
@SpringBootApplication
public class BdBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(BdBotApplication.class, args);
	}

}

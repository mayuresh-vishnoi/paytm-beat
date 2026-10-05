package com.mayur29.paytmbeat.paytmbeat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaytmBeatApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaytmBeatApplication.class, args);
	}

}

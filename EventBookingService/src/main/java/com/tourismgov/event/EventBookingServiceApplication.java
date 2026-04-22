package com.tourismgov.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class EventBookingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventBookingServiceApplication.class, args);
	}

}

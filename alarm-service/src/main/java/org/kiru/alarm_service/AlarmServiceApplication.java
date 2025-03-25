package org.kiru.alarm_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties
@EntityScan(basePackages = {"org.kiru.core.device"})
public class AlarmServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AlarmServiceApplication.class, args);
	}

}

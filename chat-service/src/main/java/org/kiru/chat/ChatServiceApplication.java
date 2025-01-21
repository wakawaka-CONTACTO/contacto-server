package org.kiru.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EntityScan(basePackages = "org.kiru.core.chat")
@EnableJpaAuditing
@EnableFeignClients
public class ChatServiceApplication {
    /**
     * Entry point for the Chat Service Spring Boot application.
     *
     * Launches the Spring Boot application with the specified command-line arguments.
     * Initializes the application context, configures components, and starts the embedded server.
     *
     * @param args Command-line arguments passed to the application during startup
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}

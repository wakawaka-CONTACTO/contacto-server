package org.kiru.chat.config.config;

import feign.RequestInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.kiru.core.exception.InvalidValueException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    @Value("${translate.api-key}")
    private String apiKey;

    @Value("${translate.api.host}")
    private String host;

    /**
     * Creates a Feign request interceptor to add custom headers to outgoing HTTP requests.
     *
     * @return A {@link RequestInterceptor} that sets standard headers for API requests
     * @see RequestInterceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("x-rapidapi-key", apiKey);
            template.header("x-rapidapi-host", host);
            template.header("Content-Type", "application/json");
        };
    }

    /**
     * Validates the configuration of the Feign client by checking the API key.
     *
     * This method is automatically called after dependency injection and ensures
     * that a valid API key is present for the translation service configuration.
     *
     * @throws InvalidValueException if the API key is null or blank, 
     *         indicating an invalid configuration
     */
    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new InvalidValueException(FailureCode.INVALID_CONFIGURATION);
        }
    }
}
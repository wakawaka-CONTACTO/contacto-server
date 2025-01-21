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

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("x-rapidapi-key", apiKey);
            template.header("x-rapidapi-host", host);
            template.header("Content-Type", "application/json");
        };
    }

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new InvalidValueException(FailureCode.INVALID_CONFIGURATION);
        }
    }
}
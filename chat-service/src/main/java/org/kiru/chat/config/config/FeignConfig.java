package org.kiru.chat.config.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    @Value("${translate.api-key:.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("x-rapidapi-key", apiKey);
            template.header("x-rapidapi-host", "ai-translate.p.rapidapi.com'");
            template.header("Content-Type", "application/json");
        };
    }
}
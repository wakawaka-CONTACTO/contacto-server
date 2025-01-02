package org.kiru.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.kiru.user.common.FeignErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {
    private final ObjectMapper objectMapper;
    private final long period = 100;
    private final long duration = 1L;
    private final int maxAttempt = 3;

    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new FeignErrorDecoder(objectMapper);
    }

    @Bean
    Retryer.Default openFeinClientRetryer() {
        return new Retryer.Default(
                period,                               // default : 100
                TimeUnit.SECONDS.toMillis(duration),  // default : 1L
                maxAttempt                            // default : 5
        );
    }
}
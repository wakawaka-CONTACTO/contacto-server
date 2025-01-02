package org.kiru.gateway.config;

import brave.http.HttpRequest;
import brave.sampler.SamplerFunction;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "docker"})
public class ZipkinConfig {
    private static List<String> WHITE_LISTED_PATHS = List.of("actuator", "eureka");

    @Bean
    public SamplerFunction<HttpRequest> customHttpSampler() {
        return request -> {
            String url = request.url();
            if (WHITE_LISTED_PATHS.stream().anyMatch(url::contains)) {
                return false;
            }
            return true;
        };
    }
}
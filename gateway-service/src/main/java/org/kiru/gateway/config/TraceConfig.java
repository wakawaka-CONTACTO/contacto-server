package org.kiru.gateway.config;

import io.micrometer.observation.ObservationPredicate;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;

@Configuration
public class TraceConfig {

    private final static String[] WHITE_LISTED_PATHS = {"/actuator", "/eureka","/discovery", "/swagger"};
    /*
     * Spring Micrometer tracing configuration
     * Includes predicates to skip tracing on non-business endpoints such as actuator and swagger
     */
    @Bean
    public ObservationPredicate noSpringSecurity() {
        return (name, context) -> !name.startsWith("spring.security.");
    }

    @Bean
    public ObservationPredicate noActuator() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext srCtx) {
                return Arrays.stream(WHITE_LISTED_PATHS)
                        .noneMatch(path -> srCtx.getCarrier().getURI().getPath().contains(path));
            }
            return true;
        };
    }
}
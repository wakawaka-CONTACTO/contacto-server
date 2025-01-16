package org.kiru.user.config;

import io.micrometer.observation.ObservationPredicate;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class TraceConfig {

    private final static String[] WHITE_LISTED_PATHS = {"actuator", "eureka", "discovery", "swagger"};


    @Bean
    public ObservationPredicate noActuator() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext srCtx) {
                return Arrays.stream(WHITE_LISTED_PATHS)
                        .noneMatch(path -> srCtx.getCarrier().getRequestURI().contains(path));
            }
            return true;
        };
    }
}
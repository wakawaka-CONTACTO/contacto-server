package org.kiru.chat.config.config;

import io.micrometer.observation.ObservationPredicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
@Slf4j
public class TraceConfig {

    /*
     * Spring Micrometer tracing configuration
     * Includes predicates to skip tracing on non-business endpoints such as actuator and swagger
     */
    private final static String[] WHITE_LISTED_PATHS = {"actuator", "eureka", "discovery", "swagger"};

    @Bean
    public ObservationPredicate noActuator() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext srCtx) {
               String url = srCtx.getCarrier().getRequestURI();
                for (String whiteListedPath : WHITE_LISTED_PATHS) {
                    if (url.contains(whiteListedPath)) {
                        return false;
                    }
                }
                return true;
            }
            return true;
        };
    }
}
package org.kiru.user.config;


import io.micrometer.observation.ObservationPredicate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
@Profile({"local", "docker"})
public class ZipkinConfig {
    private static final List<String> WHITE_LISTED_PATHS = List.of("actuator", "eureka","discovery", "swagger");

    @Bean
    ObservationPredicate noopServerRequestObservationPredicate() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext c) {
                HttpServletRequest servletRequest = c.getCarrier();
                String requestURI = servletRequest.getRequestURI();
                for (String whiteListedPath : WHITE_LISTED_PATHS) {
                    if(requestURI.contains(whiteListedPath)){
                        return false;
                    }
                }
                return WHITE_LISTED_PATHS.stream().noneMatch(requestURI::contains);
            } else if (context instanceof ClientRequestObservationContext c) {
                String uriTemplate = c.getUriTemplate();
                for (String whiteListedPath : WHITE_LISTED_PATHS) {
                    if(Objects.requireNonNull(uriTemplate).contains(whiteListedPath)){
                        return false;
                    }
                }
            }
            return true;
        };
    }
}

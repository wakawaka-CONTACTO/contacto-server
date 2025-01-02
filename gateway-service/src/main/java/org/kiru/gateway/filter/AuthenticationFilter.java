package org.kiru.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kiru.gateway.filter.AuthenticationFilter.Config;
import org.kiru.gateway.jwt.JwtUtils;
import org.kiru.gateway.jwt.JwtValidationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<Config> {
    private final JwtUtils jwtUtils;
    private final RouteValidator validator;

    public AuthenticationFilter(@Autowired JwtUtils jwtUtils,@Autowired RouteValidator validator) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.validator = validator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            ServerHttpRequest request = exchange.getRequest();
            if (validator.isSecured.test(request)) {
                if (token != null) {
                    return jwtUtils.validateToken(token)
                            .flatMap(jwtValidResponse -> {
                                if (jwtValidResponse.getStatus() == JwtValidationType.VALID_JWT) {
                                    HttpHeaders writableHeaders = HttpHeaders.writableHttpHeaders(request.getHeaders());
                                    writableHeaders.add("X-User-Id", String.valueOf(jwtValidResponse.getUser().getId()));
                                    ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(request) {
                                        @Override
                                        public HttpHeaders getHeaders() {
                                            return writableHeaders;
                                        }
                                    };
                                    ServerWebExchange modifiedExchange = exchange.mutate()
                                            .request(modifiedRequest)
                                            .build();
                                    return chain.filter(modifiedExchange);
                                }
                                return Mono.error(new RuntimeException("Invalid JWT token"));
                            })
                            .onErrorResume(e -> {
                                log.error("Error processing JWT token", e);
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            });
                }
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }

    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}

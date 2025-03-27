package org.kiru.gateway.filter;

import java.util.List;
import java.util.function.Predicate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {
    public static final List<String> openApiEndpoints = List.of(
            "/eureka",
            "/coupon/api/v1/coupon/issued-check/",
            "/api/v1/auth/login/",
            "/api/v1/auth/emailcheck",
            "/api/v1/auth/emailsend",
            "/api/v1/users/signup",
            "/api/v1/users/signin",
            "/api/v1/users/signin/help",
            "/api/v1/users/me/pwd",
            "/api/v1/users/me/email",
            "/api/v1/alarm/send/message/all"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
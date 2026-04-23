package com.tourismgov.gateway.config;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/tourismgov/v1/auth/register",
            "/tourismgov/v1/auth/login",
            "/tourismgov/v1/tourist/create",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (path.contains("/tourismgov/v1/events") && HttpMethod.GET.equals(method)) {
            if (!path.contains("/bookings")) {
                return false; // Not secured
            }
        }

        return openApiEndpoints
                .stream()
                .noneMatch(uri -> path.contains(uri)); 
    };
}
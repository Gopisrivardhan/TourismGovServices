package com.tourismgov.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authFilter;

    public GatewayConfig(AuthenticationFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        AuthenticationFilter.Config filterConfig = new AuthenticationFilter.Config();

        return builder.routes()
                
                .route("user-service-route", r -> r
                        .path("/tourismgov/v1/users/**", "/tourismgov/v1/auth/**")
                        .filters(f -> f.filter(authFilter.apply(filterConfig)))
                        .uri("lb://USER-SERVICE"))
                
                .route("event-service-route", r -> r
                        .path("/tourismgov/v1/events/**", "/tourismgov/v1/bookings/**")
                        .filters(f -> f.filter(authFilter.apply(filterConfig)))
                        .uri("lb://EVENT-SERVICE"))

                .route("tourist-service-route", r -> r
                        .path("/tourismgov/v1/tourists/**")
                        .filters(f -> f.filter(authFilter.apply(filterConfig)))
                        .uri("lb://TOURIST-SERVICE"))

                .route("notification-service-route", r -> r
                        .path("/tourismgov/v1/notifications/**", "/tourismgov/v1/audit-logs/**")
                        .filters(f -> f.filter(authFilter.apply(filterConfig)))
                        .uri("lb://NOTIFICATION-SERVICE"))

                .route("tourism-service-route", r -> r
                        .path("/tourismgov/v1/sites/**", "/tourismgov/v1/programs/**")
                        .filters(f -> f.filter(authFilter.apply(filterConfig)))
                        .uri("lb://TOURISM-SERVICE"))

                .build();
    }
}
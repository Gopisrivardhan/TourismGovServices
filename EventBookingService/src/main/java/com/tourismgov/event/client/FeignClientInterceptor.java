package com.tourismgov.event.client; // Adjust package as needed

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 1. Get the current HTTP request context
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 2. Extract the Authorization header (e.g., Bearer token)
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            
            // 3. If the token exists, attach it to the outgoing Feign request
            if (authHeader != null) {
                requestTemplate.header(AUTHORIZATION_HEADER, authHeader);
            }
        }
    }
}
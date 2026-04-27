package com.tourismgov.report.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 1. Forward the User ID
            if (request.getHeader(HEADER_USER_ID) != null) {
                template.header(HEADER_USER_ID, request.getHeader(HEADER_USER_ID));
            }
            
            // 2. Forward the Roles
            if (request.getHeader(HEADER_USER_ROLES) != null) {
                template.header(HEADER_USER_ROLES, request.getHeader(HEADER_USER_ROLES));
            }

            // 3. Forward the raw Bearer token (if your system requires it)
            if (request.getHeader(AUTHORIZATION_HEADER) != null) {
                template.header(AUTHORIZATION_HEADER, request.getHeader(AUTHORIZATION_HEADER));
            }
            
            log.debug("FeignInterceptor successfully attached security headers to the outgoing request");
        }
    }
}
package com.tourismgov.compliance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tourismgov.compliance.dto.UserResponseDto;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/tourismgov/user/users/{userId}")
    UserResponseDto getUserById(@PathVariable("userId") Long userId);

    @PostMapping("/tourismgov/user/audit-logs")
    void logAuditAction(@RequestParam("userId") Long userId, 
                        @RequestParam("action") String action, 
                        @RequestParam("resource") String resource, 
                        @RequestParam("status") String status);
}
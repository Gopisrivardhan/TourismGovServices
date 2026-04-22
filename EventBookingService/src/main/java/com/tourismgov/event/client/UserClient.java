package com.tourismgov.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @PostMapping("/tourismgov/v1/audit-logs")
    void logAction(@RequestParam("userId") Long userId, 
                   @RequestParam("action") String action, 
                   @RequestParam("resource") String resource, 
                   @RequestParam("status") String status);
}
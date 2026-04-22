package com.tourismgov.notification.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tourismgov.notification.dto.UserDTO;

@FeignClient(name = "user-service") // Eureka Service Name
public interface UserClient {
    
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/exists/{id}")
    boolean existsById(@PathVariable("id") Long id);

    @GetMapping("/api/users/internal/all")
    List<UserDTO> getAllUsers();
}
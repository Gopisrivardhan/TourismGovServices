package com.tourismgov.tourist.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tourismgov.tourist.dto.UserDTO;

// Ensure this matches the spring.application.name of your user service exactly (usually "USER-SERVICE")
@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    // Matches the @RequestMapping("/tourismgov/user/users") + @PostMapping in your UserController
    @PostMapping("/tourismgov/v1/auth/register") 
    UserDTO registerUser(@RequestBody UserDTO userDto);
    
}
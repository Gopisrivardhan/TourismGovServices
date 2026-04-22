package com.tourismgov.tourist.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tourismgov.tourist.dto.UserDTO;

@FeignClient(name = "USERSERVICE")
public interface UserClient {

    // IMPORTANT: Remove '/register' and use exactly what is in your UserController
    // Based on logs, the path should likely be:
    @PostMapping("/tourismgov/user/register") 
    UserDTO registerUser(@RequestBody UserDTO userDto);
}

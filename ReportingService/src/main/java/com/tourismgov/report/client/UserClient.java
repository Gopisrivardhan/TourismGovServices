package com.tourismgov.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tourismgov.report.dto.UserDTO;

import java.util.List;

@FeignClient(name = "tourismgov-app")
public interface UserClient {
    @GetMapping("/tourismgov/user/users")
    List<UserDTO> getAllUsers();

    @GetMapping("/tourismgov/user/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}

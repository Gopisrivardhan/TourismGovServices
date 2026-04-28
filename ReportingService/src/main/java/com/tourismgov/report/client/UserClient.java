package com.tourismgov.report.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tourismgov.report.dto.UserDTO;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/tourismgov/v1/users")
    List<UserDTO> getAllUsers();

    @GetMapping("/tourismgov/v1/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}

package com.tourismgov.notification.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.tourismgov.notification.dto.UserDTO;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/tourismgov/v1/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/tourismgov/v1/users/exists/{id}")
    boolean existsById(@PathVariable("id") Long id);

    @GetMapping("/tourismgov/v1/users")
    List<UserDTO> getAllUsers();
}
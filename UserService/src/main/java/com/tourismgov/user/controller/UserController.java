package com.tourismgov.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.user.dto.UserResponse;
import com.tourismgov.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tourismgov/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.fetchAllUsers());
    }
}
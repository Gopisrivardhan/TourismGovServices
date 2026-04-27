package com.tourismgov.user.service;

import java.util.List;

import com.tourismgov.user.dto.UserResponse;

public interface UserService {
    List<UserResponse> fetchAllUsers();
    UserResponse getUserById(Long id);
}
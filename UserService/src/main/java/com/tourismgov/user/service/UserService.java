package com.tourismgov.user.service;

import java.util.List;

import com.tourismgov.user.dto.UserRequest;
import com.tourismgov.user.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    List<UserResponse> fetchAllUsers();
}
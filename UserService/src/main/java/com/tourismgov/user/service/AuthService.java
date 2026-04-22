package com.tourismgov.user.service;

import com.tourismgov.user.dto.AuthRequest;
import com.tourismgov.user.dto.AuthResponse;
import com.tourismgov.user.dto.PasswordResetRequest;
import com.tourismgov.user.dto.PasswordUpdateRequest;
import com.tourismgov.user.dto.UserRequest;
import com.tourismgov.user.dto.UserResponse;

public interface AuthService {
    UserResponse registerUser(UserRequest request);
    AuthResponse loginUser(AuthRequest request);
    void updatePassword(PasswordUpdateRequest request);
    void resetPassword(PasswordResetRequest request);
}
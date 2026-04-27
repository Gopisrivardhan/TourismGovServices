package com.tourismgov.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.user.dto.UserResponse;
import com.tourismgov.user.entity.User;
import com.tourismgov.user.exceptions.ResourceNotFoundException; 
import com.tourismgov.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    
    // ✅ ADDED missing constant for your exception
    private static final String ENTITY_USER = "User"; 
    
    @Override
    public List<UserResponse> fetchAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }
    
    @Override
    public UserResponse getUserById(Long id) {
        log.info("Fetching user details for ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_USER, id));
                
        // ✅ FIXED: Now uses the single complete mapping method below
        return toResponse(user); 
    }

    // --- Private Utility Method for Mapping ---
    // ✅ COMBINED mapToUserDTO and toResponse into one complete method
    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        return response;
    }
}
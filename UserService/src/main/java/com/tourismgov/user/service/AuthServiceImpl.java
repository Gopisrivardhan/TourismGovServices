package com.tourismgov.user.service;

import static com.tourismgov.user.exceptions.SecurityErrorMessages.USER_NOT_FOUND;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.user.dto.AuthRequest;
import com.tourismgov.user.dto.AuthResponse;
import com.tourismgov.user.dto.PasswordResetRequest;
import com.tourismgov.user.dto.PasswordUpdateRequest;
import com.tourismgov.user.dto.UserRequest;
import com.tourismgov.user.dto.UserResponse;
import com.tourismgov.user.entity.User;
import com.tourismgov.user.enums.Status;
import com.tourismgov.user.exceptions.ResourceNotFoundException;
import com.tourismgov.user.repository.UserRepository;
import com.tourismgov.user.security.JwtUtil;
import com.tourismgov.user.security.SecurityUtils;
import com.tourismgov.user.client.TouristClient;
import com.tourismgov.user.dto.TouristSyncRequest;
import com.tourismgov.user.enums.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final String RESOURCE_AUTH_SERVICE = "AuthService";

	private static final String STATUS_SUCCESS = "SUCCESS";
	private static final String STATUS_FAILED = "FAILED";

	private static final String ACTION_USER_REGISTER = "USER_REGISTER";
	private static final String ACTION_USER_LOGIN = "USER_LOGIN";
	private static final String ACTION_UPDATE_PASSWORD = "UPDATE_PASSWORD";
	private static final String ACTION_RESET_PASSWORD = "RESET_PASSWORD";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final JwtUtil jwtUtil;
	private final AuditLogService auditLogService;
	private final TouristClient touristClient;

	// ---------------- REGISTER ----------------

	@Override
	@Transactional
	public UserResponse registerUser(UserRequest request) {

		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
		}

		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setPhone(request.getPhone());

		// ✅ ENUM ASSIGNMENT (CORRECT)
		user.setRole(request.getRole());
		user.setStatus(Status.ACTIVE);

		User savedUser = userRepository.save(user);

		auditLogService.logActionInCurrentTransaction(savedUser.getUserId(), ACTION_USER_REGISTER,
				RESOURCE_AUTH_SERVICE, STATUS_SUCCESS);

		if (savedUser.getRole() == Role.TOURIST) {
		    try {
		        touristClient.syncTouristProfile(TouristSyncRequest.builder()
		                .userId(savedUser.getUserId())
		                .name(savedUser.getName())
		                .email(savedUser.getEmail())
		                .contactInfo(savedUser.getPhone())
		                .build());
		        log.info("Successfully synced tourist profile for user {}", savedUser.getUserId());
		    } catch (Exception e) {
		        log.error("Failed to sync tourist profile for user {}: {}", savedUser.getUserId(), e.getMessage());
		    }
		}

		return mapToUserResponse(savedUser);
	}

	// ---------------- LOGIN ----------------

	@Override
	public AuthResponse loginUser(AuthRequest request) {

		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

			User user = userRepository.findByEmail(request.getEmail())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

			// ✅ ENUM COMPARISON (CORRECT)
			if (user.getStatus() != Status.ACTIVE) {
				auditLogService.logAction(user.getUserId(), ACTION_USER_LOGIN, RESOURCE_AUTH_SERVICE, STATUS_FAILED);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
			}

			UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

			String jwt = jwtUtil.generateToken(userDetails);

			auditLogService.logAction(user.getUserId(), ACTION_USER_LOGIN, RESOURCE_AUTH_SERVICE, STATUS_SUCCESS);

			// ✅ ENUM → STRING ONLY FOR RESPONSE
			return new AuthResponse(jwt, user.getUserId(), user.getRole().name(), user.getName());

		} catch (AuthenticationException ex) {

			userRepository.findByEmail(request.getEmail()).ifPresent(user -> auditLogService.logAction(user.getUserId(),
					ACTION_USER_LOGIN, RESOURCE_AUTH_SERVICE, STATUS_FAILED));

			log.warn("Failed login attempt for email: {}", request.getEmail());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}
	}

	// ---------------- UPDATE PASSWORD ----------------

	@Override
	@Transactional
	public void updatePassword(PasswordUpdateRequest request) {

		Long loggedInUserId = SecurityUtils.getCurrentUserId();

		if (!loggedInUserId.equals(request.getUserId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update only your own password");
		}

		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

		if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {

			auditLogService.logAction(user.getUserId(), ACTION_UPDATE_PASSWORD, RESOURCE_AUTH_SERVICE, STATUS_FAILED);

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));

		auditLogService.logAction(user.getUserId(), ACTION_UPDATE_PASSWORD, RESOURCE_AUTH_SERVICE, STATUS_SUCCESS);
	}

	// ---------------- RESET PASSWORD (ADMIN) ----------------

	@Override
	@Transactional
	public void resetPassword(PasswordResetRequest request) {

		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));

		auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_RESET_PASSWORD, RESOURCE_AUTH_SERVICE,
				STATUS_SUCCESS);
	}

	// ---------------- MAPPER ----------------

	private UserResponse mapToUserResponse(User user) {
		UserResponse dto = new UserResponse();
		dto.setUserId(user.getUserId());
		dto.setName(user.getName());
		dto.setEmail(user.getEmail());

		// ✅ PASS ENUMS DIRECTLY
		dto.setRole(user.getRole());
		dto.setStatus(user.getStatus());

		dto.setPhone(user.getPhone());
		dto.setCreatedAt(user.getCreatedAt());
		dto.setUpdatedAt(user.getUpdatedAt());
		return dto;
	}

}
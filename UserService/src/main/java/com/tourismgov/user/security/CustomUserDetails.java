package com.tourismgov.user.security;


import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tourismgov.user.entity.User;
import com.tourismgov.user.enums.Status;

public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	private final Long userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isActive;

    public CustomUserDetails(User user) {
        this.userId = user.getUserId();
        this.username = user.getEmail(); 
        this.password = user.getPassword();
        
        // Convert your DB role ("ADMIN", "TOURIST") into a Spring Security role ("ROLE_ADMIN")
        this.authorities = Collections.singletonList(
        		new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        
        // Assuming your user table has a status. If not, just set to true.
        this.isActive = user.getStatus() == Status.ACTIVE;
    }

    // --- The crucial getter for your Audit Logs ---
    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; } 
}
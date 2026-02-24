package com.pm.userservice.security;

import com.pm.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService for loading user details from database
 * Supports multi-tenant lookups
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailAndEnterpriseId(email, extractEnterpriseId(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Load user by email and enterprise ID (multi-tenant)
     */
    public UserDetails loadUserByEmailAndEnterprise(String email, String enterpriseId) throws UsernameNotFoundException {
        return userRepository.findByEmailAndEnterpriseId(email, enterpriseId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email + " in enterprise: " + enterpriseId));
    }

    /**
     * Extract enterprise ID from email context (can be enhanced with ThreadLocal)
     */
    private String extractEnterpriseId(String email) {
        // This is a placeholder - in production, use ThreadLocal or Security Context
        return "default-enterprise";
    }
}


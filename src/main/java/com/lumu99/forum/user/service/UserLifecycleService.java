package com.lumu99.forum.user.service;

import com.lumu99.forum.auth.security.JwtService;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserLifecycleService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserLifecycleService(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResult login(String username, String password) {
        UserRepository.UserRecord user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.UNAUTHORIZED,
                        "LOGIN_401_INVALID_CREDENTIALS",
                        "Invalid username or password"
                ));

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "LOGIN_401_INVALID_CREDENTIALS",
                    "Invalid username or password"
            );
        }

        if ("BANNED".equals(user.status())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "LOGIN_403_ACCOUNT_BANNED",
                    "Account is banned"
            );
        }

        if ("DEACTIVATED".equals(user.status())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "LOGIN_403_ACCOUNT_DEACTIVATED",
                    "Account is deactivated"
            );
        }

        String token = jwtService.generateToken(user.userUuid(), user.role());
        return new LoginResult(token, user.userUuid(), user.role());
    }

    public void logout() {
        // Stateless JWT logout is client-side token discard in V1.
    }

    public void changeUsername(String userUuid, String newUsername) {
        if (userRepository.existsByUsername(newUsername)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "REG_409_USERNAME_EXISTS",
                    "Username already exists"
            );
        }
        int updated = userRepository.updateUsername(userUuid, newUsername);
        if (updated == 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
    }

    public void changePassword(String userUuid, String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        int updated = userRepository.updatePasswordHash(userUuid, passwordHash);
        if (updated == 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
    }

    public void deactivate(String userUuid) {
        int updated = userRepository.updateStatus(userUuid, "DEACTIVATED");
        if (updated == 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
    }

    public String currentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String userUuid && StringUtils.hasText(userUuid) && !"anonymousUser".equals(userUuid)) {
            return userUuid;
        }
        throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
    }

    public record LoginResult(String token, String userUuid, String role) {
    }
}

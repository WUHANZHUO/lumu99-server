package com.lumu99.forum.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.auth.security.JwtService;
import com.lumu99.forum.common.enums.UserStatus;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.common.security.SecurityContextHelper;
import com.lumu99.forum.domain.User;
import com.lumu99.forum.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserLifecycleService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserLifecycleService(UserMapper userMapper,
                                PasswordEncoder passwordEncoder,
                                JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResult login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "LOGIN_401_INVALID_CREDENTIALS", "Invalid username or password");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "LOGIN_403_ACCOUNT_BANNED", "Account is banned");
        }
        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "LOGIN_403_ACCOUNT_DEACTIVATED", "Account is deactivated");
        }
        String token = jwtService.generateToken(user.getUserUuid(), user.getRole().name());
        return new LoginResult(token, user.getUserUuid(), user.getRole().name());
    }

    public void logout() {
        // Stateless JWT logout is client-side token discard in V1.
    }

    public void changeUsername(String userUuid, String newUsername) {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, newUsername)) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_USERNAME_EXISTS", "Username already exists");
        }
        User user = requireUser(userUuid);
        user.setUsername(newUsername);
        userMapper.updateById(user);
    }

    public void changePassword(String userUuid, String newPassword) {
        User user = requireUser(userUuid);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public void deactivate(String userUuid) {
        User user = requireUser(userUuid);
        user.setStatus(UserStatus.DEACTIVATED);
        userMapper.updateById(user);
    }

    public String currentUserUuid() {
        return SecurityContextHelper.currentUserUuid();
    }

    private User requireUser(String userUuid) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserUuid, userUuid));
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        return user;
    }

    public record LoginResult(String token, String userUuid, String role) {}
}

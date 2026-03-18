package com.lumu99.forum.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.common.enums.MuteStatus;
import com.lumu99.forum.common.enums.UserStatus;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.domain.User;
import com.lumu99.forum.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User ban(String userUuid, boolean banned) {
        User user = requireUser(userUuid);
        user.setStatus(banned ? UserStatus.BANNED : UserStatus.ACTIVE);
        userMapper.updateById(user);
        return user;
    }

    public User mute(String userUuid, boolean muted) {
        User user = requireUser(userUuid);
        user.setMuteStatus(muted ? MuteStatus.MUTED : MuteStatus.NORMAL);
        userMapper.updateById(user);
        return user;
    }

    public User resetUsername(String userUuid, String newUsername) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, newUsername));
        if (existing != null && !existing.getUserUuid().equals(userUuid)) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_USERNAME_EXISTS", "Username already exists");
        }
        User user = requireUser(userUuid);
        user.setUsername(newUsername);
        userMapper.updateById(user);
        return user;
    }

    public void resetPassword(String userUuid, String newPassword) {
        User user = requireUser(userUuid);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private User requireUser(String userUuid) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserUuid, userUuid));
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "User not found");
        }
        return user;
    }
}

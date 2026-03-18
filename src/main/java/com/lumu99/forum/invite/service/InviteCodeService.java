package com.lumu99.forum.invite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lumu99.forum.auth.security.JwtService;
import com.lumu99.forum.common.enums.InviteCodeStatus;
import com.lumu99.forum.common.enums.MuteStatus;
import com.lumu99.forum.common.enums.UserRole;
import com.lumu99.forum.common.enums.UserStatus;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.domain.InviteCode;
import com.lumu99.forum.domain.User;
import com.lumu99.forum.mapper.InviteCodeMapper;
import com.lumu99.forum.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InviteCodeService {

    private final InviteCodeMapper inviteCodeMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public InviteCodeService(InviteCodeMapper inviteCodeMapper,
                             UserMapper userMapper,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService) {
        this.inviteCodeMapper = inviteCodeMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegistrationResult registerByInvite(InviteRegistrationCommand command) {
        InviteCode inviteCode = inviteCodeMapper.selectOne(
                new LambdaQueryWrapper<InviteCode>().eq(InviteCode::getCode, command.inviteCode())
        );
        if (inviteCode == null || inviteCode.getStatus() != InviteCodeStatus.UNUSED) {
            throw invalidInviteCode();
        }
        if (inviteCode.getExpiresAt() != null && inviteCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw invalidInviteCode();
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, command.username())) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_USERNAME_EXISTS", "Username already exists");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getWeiboName, command.weiboName())) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_WEIBO_EXISTS", "Weibo name already exists");
        }

        String userUuid = UUID.randomUUID().toString();
        User user = new User();
        user.setUserUuid(userUuid);
        user.setUsername(command.username());
        user.setWeiboName(command.weiboName());
        user.setPasswordHash(passwordEncoder.encode(command.password()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setMuteStatus(MuteStatus.NORMAL);
        userMapper.insert(user);

        int updated = inviteCodeMapper.update(null,
                new LambdaUpdateWrapper<InviteCode>()
                        .eq(InviteCode::getId, inviteCode.getId())
                        .eq(InviteCode::getStatus, InviteCodeStatus.UNUSED)
                        .set(InviteCode::getStatus, InviteCodeStatus.USED)
                        .set(InviteCode::getUsedByUserUuid, userUuid)
                        .set(InviteCode::getUsedAt, LocalDateTime.now())
        );
        if (updated == 0) {
            throw invalidInviteCode();
        }

        String token = jwtService.generateToken(userUuid, UserRole.USER.name());
        return new RegistrationResult(userUuid, token);
    }

    public InviteCode createInviteCode(LocalDateTime expiresAt) {
        String code = "INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode(code);
        inviteCode.setStatus(InviteCodeStatus.UNUSED);
        inviteCode.setExpiresAt(expiresAt);
        inviteCodeMapper.insert(inviteCode);
        return inviteCode;
    }

    public List<InviteCode> listInviteCodes() {
        return inviteCodeMapper.selectList(new LambdaQueryWrapper<InviteCode>().orderByDesc(InviteCode::getId));
    }

    private BusinessException invalidInviteCode() {
        return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "REG_422_INVITE_CODE_INVALID", "Invite code is invalid");
    }

    public record InviteRegistrationCommand(String username, String password, String weiboName, String inviteCode) {}

    public record RegistrationResult(String userUuid, String token) {}
}

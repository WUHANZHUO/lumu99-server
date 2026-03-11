package com.lumu99.forum.invite.service;

import com.lumu99.forum.auth.security.JwtService;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.invite.repository.InviteCodeRepository;
import com.lumu99.forum.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public InviteCodeService(InviteCodeRepository inviteCodeRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegistrationResult registerByInvite(InviteRegistrationCommand command) {
        InviteCodeRepository.InviteCodeRecord inviteCode = inviteCodeRepository.findByCode(command.inviteCode())
                .orElseThrow(this::invalidInviteCode);

        if (!"UNUSED".equals(inviteCode.status())) {
            throw invalidInviteCode();
        }
        if (inviteCode.expiresAt() != null && inviteCode.expiresAt().isBefore(Instant.now())) {
            throw invalidInviteCode();
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_USERNAME_EXISTS", "Username already exists");
        }
        if (userRepository.existsByWeiboName(command.weiboName())) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_WEIBO_EXISTS", "Weibo name already exists");
        }

        String userUuid = UUID.randomUUID().toString();
        String hash = passwordEncoder.encode(command.password());
        userRepository.createUser(userUuid, command.username(), command.weiboName(), hash, "USER", "ACTIVE", "NORMAL");

        int updated = inviteCodeRepository.markUsed(inviteCode.id(), userUuid, Instant.now());
        if (updated == 0) {
            throw invalidInviteCode();
        }

        String token = jwtService.generateToken(userUuid, "USER");
        return new RegistrationResult(userUuid, token);
    }

    public InviteCodeView createInviteCode(Instant expiresAt) {
        String code = "INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        InviteCodeRepository.InviteCodeRecord created = inviteCodeRepository.createInviteCode(code, expiresAt);
        return new InviteCodeView(created.id(), created.code(), created.status(), created.expiresAt(), created.usedByUserUuid(), created.usedAt());
    }

    public List<InviteCodeView> listInviteCodes() {
        return inviteCodeRepository.listAll().stream()
                .map(row -> new InviteCodeView(row.id(), row.code(), row.status(), row.expiresAt(), row.usedByUserUuid(), row.usedAt()))
                .toList();
    }

    private BusinessException invalidInviteCode() {
        return new BusinessException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "REG_422_INVITE_CODE_INVALID",
                "Invite code is invalid"
        );
    }

    public record InviteRegistrationCommand(String username, String password, String weiboName, String inviteCode) {
    }

    public record RegistrationResult(String userUuid, String token) {
    }

    public record InviteCodeView(Long id,
                                 String code,
                                 String status,
                                 Instant expiresAt,
                                 String usedByUserUuid,
                                 Instant usedAt) {
    }
}

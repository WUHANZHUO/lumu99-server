package com.lumu99.forum.admin.controller;

import com.lumu99.forum.admin.service.AdminUserService;
import com.lumu99.forum.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminUserGovernanceController {

    private final AdminUserService adminUserService;

    public AdminUserGovernanceController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PutMapping("/{userUuid}/ban")
    public ResponseEntity<Map<String, Object>> ban(@PathVariable String userUuid,
                                                    @Valid @RequestBody BanRequest request) {
        User user = adminUserService.ban(userUuid, request.banned());
        return ResponseEntity.ok(Map.of("data", Map.of("userUuid", userUuid, "status", user.getStatus().name())));
    }

    @PutMapping("/{userUuid}/mute")
    public ResponseEntity<Map<String, Object>> mute(@PathVariable String userUuid,
                                                     @Valid @RequestBody MuteRequest request) {
        User user = adminUserService.mute(userUuid, request.muted());
        return ResponseEntity.ok(Map.of("data", Map.of("userUuid", userUuid, "muteStatus", user.getMuteStatus().name())));
    }

    @PutMapping("/{userUuid}/reset-username")
    public ResponseEntity<Map<String, Object>> resetUsername(@PathVariable String userUuid,
                                                              @Valid @RequestBody ResetUsernameRequest request) {
        User user = adminUserService.resetUsername(userUuid, request.newUsername());
        return ResponseEntity.ok(Map.of("data", Map.of("userUuid", userUuid, "username", user.getUsername())));
    }

    @PutMapping("/{userUuid}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable String userUuid,
                                                              @Valid @RequestBody ResetPasswordRequest request) {
        adminUserService.resetPassword(userUuid, request.newPassword());
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    public record BanRequest(@NotNull Boolean banned) {}
    public record MuteRequest(@NotNull Boolean muted) {}
    public record ResetUsernameRequest(@NotBlank String newUsername) {}
    public record ResetPasswordRequest(@NotBlank String newPassword) {}
}

package com.lumu99.forum.user.controller;

import com.lumu99.forum.user.service.UserLifecycleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users/me")
public class UserController {

    private final UserLifecycleService userLifecycleService;

    public UserController(UserLifecycleService userLifecycleService) {
        this.userLifecycleService = userLifecycleService;
    }

    @PutMapping("/username")
    public ResponseEntity<Map<String, Object>> changeUsername(@Valid @RequestBody ChangeUsernameRequest request) {
        userLifecycleService.changeUsername(userLifecycleService.currentUserUuid(), request.newUsername());
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userLifecycleService.changePassword(userLifecycleService.currentUserUuid(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deactivate() {
        userLifecycleService.deactivate(userLifecycleService.currentUserUuid());
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    public record ChangeUsernameRequest(@NotBlank String newUsername) {
    }

    public record ChangePasswordRequest(@NotBlank String newPassword) {
    }
}

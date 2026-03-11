package com.lumu99.forum.auth.controller;

import com.lumu99.forum.user.service.UserLifecycleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserLifecycleService userLifecycleService;

    public AuthController(UserLifecycleService userLifecycleService) {
        this.userLifecycleService = userLifecycleService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        UserLifecycleService.LoginResult result = userLifecycleService.login(request.username(), request.password());
        return ResponseEntity.ok(Map.of("data", result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        userLifecycleService.logout();
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }
}

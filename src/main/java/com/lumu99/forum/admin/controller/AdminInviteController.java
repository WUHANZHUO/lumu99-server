package com.lumu99.forum.admin.controller;

import com.lumu99.forum.invite.service.InviteCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/admin/invite-codes")
public class AdminInviteController {

    private final InviteCodeService inviteCodeService;

    public AdminInviteController(InviteCodeService inviteCodeService) {
        this.inviteCodeService = inviteCodeService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createInviteCode(@RequestBody(required = false) CreateInviteCodeRequest request) {
        Instant expiresAt = null;
        if (request != null && StringUtils.hasText(request.expiresAt())) {
            expiresAt = Instant.parse(request.expiresAt());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("data", inviteCodeService.createInviteCode(expiresAt)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listInviteCodes() {
        return ResponseEntity.ok(Map.of("data", inviteCodeService.listInviteCodes()));
    }

    public record CreateInviteCodeRequest(String expiresAt) {
    }
}

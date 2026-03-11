package com.lumu99.forum.invite.controller;

import com.lumu99.forum.invite.service.InviteCodeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/register")
public class InviteRegistrationController {

    private final InviteCodeService inviteCodeService;

    public InviteRegistrationController(InviteCodeService inviteCodeService) {
        this.inviteCodeService = inviteCodeService;
    }

    @PostMapping("/invite")
    public ResponseEntity<Map<String, Object>> registerByInvite(@Valid @RequestBody InviteRegisterRequest request) {
        InviteCodeService.RegistrationResult result = inviteCodeService.registerByInvite(
                new InviteCodeService.InviteRegistrationCommand(
                        request.username(),
                        request.password(),
                        request.weiboName(),
                        request.inviteCode()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", result));
    }

    public record InviteRegisterRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String weiboName,
            @NotBlank String inviteCode
    ) {
    }
}

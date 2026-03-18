package com.lumu99.forum.admin.controller;

import com.lumu99.forum.admin.service.AdminSettingsService;
import com.lumu99.forum.domain.AdminSettings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        AdminSettings settings = adminSettingsService.getSettings();
        return ResponseEntity.ok(Map.of("data", new SettingsResponse(
                Boolean.TRUE.equals(settings.getWorldGuestVisible()),
                Boolean.TRUE.equals(settings.getEventsGuestVisible()),
                Boolean.TRUE.equals(settings.getForumPostNeedReview()),
                Boolean.TRUE.equals(settings.getUserDmEnabled())
        )));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@Valid @RequestBody UpdateSettingsRequest request) {
        AdminSettings updated = adminSettingsService.updateSettings(
                request.worldGuestVisible(),
                request.eventsGuestVisible(),
                request.forumPostNeedReview(),
                request.userDmEnabled()
        );
        return ResponseEntity.ok(Map.of("data", new SettingsResponse(
                Boolean.TRUE.equals(updated.getWorldGuestVisible()),
                Boolean.TRUE.equals(updated.getEventsGuestVisible()),
                Boolean.TRUE.equals(updated.getForumPostNeedReview()),
                Boolean.TRUE.equals(updated.getUserDmEnabled())
        )));
    }

    public record UpdateSettingsRequest(
            @NotNull Boolean worldGuestVisible,
            @NotNull Boolean eventsGuestVisible,
            @NotNull Boolean forumPostNeedReview,
            @NotNull Boolean userDmEnabled
    ) {}

    public record SettingsResponse(
            boolean worldGuestVisible,
            boolean eventsGuestVisible,
            boolean forumPostNeedReview,
            boolean userDmEnabled
    ) {}
}

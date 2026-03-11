package com.lumu99.forum.admin.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    private final JdbcTemplate jdbcTemplate;

    public AdminSettingsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT world_guest_visible, events_guest_visible, forum_post_need_review, user_dm_enabled FROM admin_settings WHERE id = 1"
        );
        return ResponseEntity.ok(Map.of(
                "data", new SettingsResponse(
                        toBoolean(row.get("world_guest_visible")),
                        toBoolean(row.get("events_guest_visible")),
                        toBoolean(row.get("forum_post_need_review")),
                        toBoolean(row.get("user_dm_enabled"))
                )
        ));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@Valid @RequestBody UpdateSettingsRequest request) {
        jdbcTemplate.update(
                "UPDATE admin_settings SET world_guest_visible=?, events_guest_visible=?, forum_post_need_review=?, user_dm_enabled=? WHERE id = 1",
                request.worldGuestVisible(),
                request.eventsGuestVisible(),
                request.forumPostNeedReview(),
                request.userDmEnabled()
        );
        return ResponseEntity.ok(Map.of("data", request));
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public record UpdateSettingsRequest(
            @NotNull Boolean worldGuestVisible,
            @NotNull Boolean eventsGuestVisible,
            @NotNull Boolean forumPostNeedReview,
            @NotNull Boolean userDmEnabled
    ) {
    }

    public record SettingsResponse(
            boolean worldGuestVisible,
            boolean eventsGuestVisible,
            boolean forumPostNeedReview,
            boolean userDmEnabled
    ) {
    }
}

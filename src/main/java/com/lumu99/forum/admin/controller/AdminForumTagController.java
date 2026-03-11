package com.lumu99.forum.admin.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/forum-tags")
public class AdminForumTagController {

    private final JdbcTemplate jdbcTemplate;

    public AdminForumTagController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<ForumTagResponse> tags = jdbcTemplate.query(
                "SELECT id, name, admin_only, enabled FROM forum_tags ORDER BY id DESC",
                (rs, rowNum) -> new ForumTagResponse(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getBoolean("admin_only"),
                        rs.getBoolean("enabled")
                )
        );
        return ResponseEntity.ok(Map.of("data", tags));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ForumTagRequest request) {
        jdbcTemplate.update(
                "INSERT INTO forum_tags (name, admin_only, enabled) VALUES (?, ?, ?)",
                request.name(),
                request.adminOnly(),
                request.enabled()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody ForumTagRequest request) {
        jdbcTemplate.update(
                "UPDATE forum_tags SET name = ?, admin_only = ?, enabled = ? WHERE id = ?",
                request.name(),
                request.adminOnly(),
                request.enabled(),
                id
        );
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM forum_tags WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    public record ForumTagRequest(
            @NotBlank String name,
            @NotNull Boolean adminOnly,
            @NotNull Boolean enabled
    ) {
    }

    public record ForumTagResponse(
            Long id,
            String name,
            boolean adminOnly,
            boolean enabled
    ) {
    }
}

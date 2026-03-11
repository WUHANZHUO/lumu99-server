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
@RequestMapping("/admin/forbidden-words")
public class AdminForbiddenWordController {

    private final JdbcTemplate jdbcTemplate;

    public AdminForbiddenWordController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<ForbiddenWordResponse> words = jdbcTemplate.query(
                "SELECT id, word, enabled FROM forbidden_words ORDER BY id DESC",
                (rs, rowNum) -> new ForbiddenWordResponse(
                        rs.getLong("id"),
                        rs.getString("word"),
                        rs.getBoolean("enabled")
                )
        );
        return ResponseEntity.ok(Map.of("data", words));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ForbiddenWordRequest request) {
        jdbcTemplate.update(
                "INSERT INTO forbidden_words (word, enabled) VALUES (?, ?)",
                request.word(),
                request.enabled()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody ForbiddenWordRequest request) {
        jdbcTemplate.update(
                "UPDATE forbidden_words SET word = ?, enabled = ? WHERE id = ?",
                request.word(),
                request.enabled(),
                id
        );
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM forbidden_words WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    public record ForbiddenWordRequest(
            @NotBlank String word,
            @NotNull Boolean enabled
    ) {
    }

    public record ForbiddenWordResponse(
            Long id,
            String word,
            boolean enabled
    ) {
    }
}

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
@RequestMapping("/admin/quiz")
public class AdminQuizController {

    private final JdbcTemplate jdbcTemplate;

    public AdminQuizController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/questions")
    public ResponseEntity<Map<String, Object>> listQuestions() {
        List<QuizQuestionResponse> questions = jdbcTemplate.query(
                "SELECT id, question_type, stem, answer_text, enabled FROM quiz_questions ORDER BY id DESC",
                (rs, rowNum) -> new QuizQuestionResponse(
                        rs.getLong("id"),
                        rs.getString("question_type"),
                        rs.getString("stem"),
                        rs.getString("answer_text"),
                        rs.getBoolean("enabled")
                )
        );
        return ResponseEntity.ok(Map.of("data", questions));
    }

    @PostMapping("/questions")
    public ResponseEntity<Map<String, Object>> createQuestion(@Valid @RequestBody QuizQuestionRequest request) {
        jdbcTemplate.update(
                "INSERT INTO quiz_questions (question_type, stem, answer_text, enabled) VALUES (?, ?, ?, ?)",
                request.questionType(),
                request.stem(),
                request.answerText(),
                request.enabled()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "OK"));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<Map<String, Object>> updateQuestion(@PathVariable Long id,
                                                              @Valid @RequestBody QuizQuestionRequest request) {
        jdbcTemplate.update(
                "UPDATE quiz_questions SET question_type=?, stem=?, answer_text=?, enabled=? WHERE id = ?",
                request.questionType(),
                request.stem(),
                request.answerText(),
                request.enabled(),
                id
        );
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM quiz_options WHERE question_id = ?", id);
        jdbcTemplate.update("DELETE FROM quiz_questions WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@Valid @RequestBody QuizConfigRequest request) {
        jdbcTemplate.update(
                "UPDATE quiz_config SET question_count = ?, pass_score = ? WHERE id = 1",
                request.questionCount(),
                request.passScore()
        );
        return ResponseEntity.ok(Map.of("data", request));
    }

    public record QuizQuestionRequest(
            @NotBlank String questionType,
            @NotBlank String stem,
            @NotBlank String answerText,
            @NotNull Boolean enabled
    ) {
    }

    public record QuizQuestionResponse(
            Long id,
            String questionType,
            String stem,
            String answerText,
            boolean enabled
    ) {
    }

    public record QuizConfigRequest(
            @NotNull Integer questionCount,
            @NotNull Integer passScore
    ) {
    }
}

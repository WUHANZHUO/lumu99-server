package com.lumu99.forum.quiz.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class QuizRepository {

    private final JdbcTemplate jdbcTemplate;

    public QuizRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int currentQuestionCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT question_count FROM quiz_config WHERE id = 1",
                Integer.class
        );
        return count == null ? 0 : count;
    }

    public List<QuizQuestion> randomEnabledQuestions(int count) {
        return jdbcTemplate.query(
                "SELECT id, question_type, stem, answer_text FROM quiz_questions WHERE enabled = true ORDER BY RAND() LIMIT ?",
                (rs, rowNum) -> new QuizQuestion(
                        rs.getLong("id"),
                        rs.getString("question_type"),
                        rs.getString("stem"),
                        rs.getString("answer_text")
                ),
                count
        );
    }

    public Optional<QuizQuestion> findEnabledQuestionById(Long questionId) {
        List<QuizQuestion> list = jdbcTemplate.query(
                "SELECT id, question_type, stem, answer_text FROM quiz_questions WHERE id = ? AND enabled = true",
                (rs, rowNum) -> new QuizQuestion(
                        rs.getLong("id"),
                        rs.getString("question_type"),
                        rs.getString("stem"),
                        rs.getString("answer_text")
                ),
                questionId
        );
        return list.stream().findFirst();
    }

    public List<QuizOption> findOptionsByQuestionId(Long questionId) {
        return jdbcTemplate.query(
                "SELECT option_key, option_text, is_correct FROM quiz_options WHERE question_id = ?",
                (rs, rowNum) -> new QuizOption(
                        rs.getString("option_key"),
                        rs.getString("option_text"),
                        rs.getBoolean("is_correct")
                ),
                questionId
        );
    }

    public record QuizQuestion(Long id, String questionType, String stem, String answerText) {
    }

    public record QuizOption(String optionKey, String optionText, boolean correct) {
    }
}

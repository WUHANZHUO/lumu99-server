package com.lumu99.forum.review.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class ReviewDecisionEngine {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDecisionEngine(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String decideReviewStatus(String operatorRole, String title, String content) {
        if (hitForbiddenWord(title, content)) {
            return "PENDING";
        }

        if ("ADMIN".equalsIgnoreCase(operatorRole)) {
            return "APPROVED";
        }

        boolean userPostNeedReview = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT forum_post_need_review FROM admin_settings WHERE id = 1",
                Boolean.class
        ));
        if (userPostNeedReview) {
            return "PENDING";
        }
        return "APPROVED";
    }

    private boolean hitForbiddenWord(String title, String content) {
        String plain = (safe(title) + " " + safe(content)).toLowerCase(Locale.ROOT);
        List<String> words = jdbcTemplate.query(
                "SELECT word FROM forbidden_words WHERE enabled = true",
                (rs, rowNum) -> rs.getString("word")
        );
        for (String word : words) {
            if (StringUtils.hasText(word) && plain.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}

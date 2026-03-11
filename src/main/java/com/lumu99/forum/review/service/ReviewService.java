package com.lumu99.forum.review.service;

import com.lumu99.forum.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final JdbcTemplate jdbcTemplate;

    public ReviewService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReviewPostView> listPendingPosts() {
        return jdbcTemplate.query(
                "SELECT id, author_uuid, title, content, review_status, reject_reason FROM forum_posts WHERE review_status = 'PENDING' ORDER BY id DESC",
                (rs, rowNum) -> new ReviewPostView(
                        rs.getLong("id"),
                        rs.getString("author_uuid"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("review_status"),
                        rs.getString("reject_reason")
                )
        );
    }

    public Optional<ReviewPostView> getPost(Long postId) {
        List<ReviewPostView> list = jdbcTemplate.query(
                "SELECT id, author_uuid, title, content, review_status, reject_reason FROM forum_posts WHERE id = ?",
                (rs, rowNum) -> new ReviewPostView(
                        rs.getLong("id"),
                        rs.getString("author_uuid"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("review_status"),
                        rs.getString("reject_reason")
                ),
                postId
        );
        return list.stream().findFirst();
    }

    public ReviewPostView approve(Long postId) {
        ensurePending(postId);
        jdbcTemplate.update(
                "UPDATE forum_posts SET review_status = 'APPROVED', reject_reason = NULL, published_at = ? WHERE id = ?",
                Timestamp.from(Instant.now()),
                postId
        );
        return getPost(postId).orElseThrow(() -> postNotPending());
    }

    public ReviewPostView reject(Long postId, String reason) {
        ensurePending(postId);
        jdbcTemplate.update(
                "UPDATE forum_posts SET review_status = 'REJECTED', reject_reason = ? WHERE id = ?",
                reason,
                postId
        );
        return getPost(postId).orElseThrow(() -> postNotPending());
    }

    private void ensurePending(Long postId) {
        String status = jdbcTemplate.queryForObject(
                "SELECT review_status FROM forum_posts WHERE id = ?",
                String.class,
                postId
        );
        if (!"PENDING".equals(status)) {
            throw postNotPending();
        }
    }

    private BusinessException postNotPending() {
        return new BusinessException(HttpStatus.NOT_FOUND, "REVIEW_404_POST_NOT_PENDING", "Post is not pending review");
    }

    public record ReviewPostView(
            Long id,
            String authorUuid,
            String title,
            String content,
            String reviewStatus,
            String rejectReason
    ) {
    }
}

package com.lumu99.forum.forum.service;

import com.lumu99.forum.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;

@Service
public class ForumCommentService {

    private final JdbcTemplate jdbcTemplate;

    public ForumCommentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CommentView> listComments(Long postId) {
        return jdbcTemplate.query(
                "SELECT id, post_id, author_uuid, content FROM forum_comments WHERE post_id = ? ORDER BY id ASC",
                (rs, rowNum) -> new CommentView(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("author_uuid"),
                        rs.getString("content")
                ),
                postId
        );
    }

    public CommentView createComment(Long postId, String content) {
        String userUuid = currentUserUuid();
        enforceNotMuted(userUuid);
        ensurePostExists(postId);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO forum_comments (post_id, author_uuid, content) VALUES (?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, postId);
            ps.setString(2, userUuid);
            ps.setString(3, content);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        if (id == null) {
            throw new IllegalStateException("Failed to create comment");
        }
        return new CommentView(id, postId, userUuid, content);
    }

    private void ensurePostExists(Long postId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM forum_posts WHERE id = ?",
                Integer.class,
                postId
        );
        if (count == null || count == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found");
        }
    }

    private void enforceNotMuted(String userUuid) {
        String muteStatus = jdbcTemplate.query(
                "SELECT mute_status FROM users WHERE user_uuid = ?",
                rs -> rs.next() ? rs.getString("mute_status") : null,
                userUuid
        );
        if ("MUTED".equals(muteStatus)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORUM_403_MUTED", "Muted user cannot comment");
        }
    }

    private String currentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        return String.valueOf(authentication.getPrincipal());
    }

    public record CommentView(Long id, Long postId, String authorUuid, String content) {
    }
}

package com.lumu99.forum.forum.service;

import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.review.service.ReviewDecisionEngine;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForumPostService {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewDecisionEngine reviewDecisionEngine;

    public ForumPostService(JdbcTemplate jdbcTemplate, ReviewDecisionEngine reviewDecisionEngine) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewDecisionEngine = reviewDecisionEngine;
    }

    public PostView createPost(CreatePostCommand command) {
        CurrentUser user = currentUser();
        validateTagPolicy(command.tagIds(), user.role());

        String reviewStatus = reviewDecisionEngine.decideReviewStatus(user.role(), command.title(), command.content());
        Timestamp publishedAt = "APPROVED".equals(reviewStatus) ? Timestamp.from(Instant.now()) : null;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO forum_posts (author_uuid, title, content, review_status, is_pinned, published_at) VALUES (?, ?, ?, ?, false, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, user.userUuid());
            ps.setString(2, command.title());
            ps.setString(3, command.content());
            ps.setString(4, reviewStatus);
            ps.setTimestamp(5, publishedAt);
            return ps;
        }, keyHolder);

        Long postId = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        if (postId == null) {
            throw new IllegalStateException("Failed to create post");
        }

        for (Long tagId : command.tagIds()) {
            jdbcTemplate.update("INSERT INTO forum_post_tag_rel (post_id, tag_id) VALUES (?, ?)", postId, tagId);
        }

        return getPost(postId).orElseThrow(() -> new IllegalStateException("Created post not found"));
    }

    public List<PostView> listPosts() {
        return jdbcTemplate.query(
                "SELECT id, author_uuid, title, content, review_status, is_pinned FROM forum_posts ORDER BY is_pinned DESC, id DESC",
                (rs, rowNum) -> new PostView(
                        rs.getLong("id"),
                        rs.getString("author_uuid"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("review_status"),
                        rs.getBoolean("is_pinned")
                )
        );
    }

    public Optional<PostView> getPost(Long postId) {
        List<PostView> posts = jdbcTemplate.query(
                "SELECT id, author_uuid, title, content, review_status, is_pinned FROM forum_posts WHERE id = ?",
                (rs, rowNum) -> new PostView(
                        rs.getLong("id"),
                        rs.getString("author_uuid"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("review_status"),
                        rs.getBoolean("is_pinned")
                ),
                postId
        );
        return posts.stream().findFirst();
    }

    public PostView updatePost(Long postId, UpdatePostCommand command) {
        CurrentUser user = currentUser();
        validateTagPolicy(command.tagIds(), user.role());

        int updated = jdbcTemplate.update(
                "UPDATE forum_posts SET title = ?, content = ? WHERE id = ?",
                command.title(),
                command.content(),
                postId
        );
        if (updated == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found");
        }
        jdbcTemplate.update("DELETE FROM forum_post_tag_rel WHERE post_id = ?", postId);
        for (Long tagId : command.tagIds()) {
            jdbcTemplate.update("INSERT INTO forum_post_tag_rel (post_id, tag_id) VALUES (?, ?)", postId, tagId);
        }
        return getPost(postId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found"));
    }

    public void deletePost(Long postId) {
        jdbcTemplate.update("DELETE FROM forum_post_tag_rel WHERE post_id = ?", postId);
        jdbcTemplate.update("DELETE FROM forum_posts WHERE id = ?", postId);
    }

    public PostView pinPost(Long postId) {
        CurrentUser user = currentUser();
        requireAdmin(user.role());
        jdbcTemplate.update("UPDATE forum_posts SET is_pinned = true WHERE id = ?", postId);
        return getPost(postId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found"));
    }

    public PostView unpinPost(Long postId) {
        CurrentUser user = currentUser();
        requireAdmin(user.role());
        jdbcTemplate.update("UPDATE forum_posts SET is_pinned = false WHERE id = ?", postId);
        return getPost(postId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found"));
    }

    private void validateTagPolicy(List<Long> tagIds, String role) {
        if (tagIds == null || tagIds.isEmpty() || "ADMIN".equals(role)) {
            return;
        }
        String placeholders = tagIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, admin_only FROM forum_tags WHERE id IN (" + placeholders + ")";
        List<Map<String, Object>> tags = jdbcTemplate.queryForList(sql, tagIds.toArray());
        for (Map<String, Object> tag : tags) {
            Object adminOnly = tag.get("admin_only");
            boolean isAdminOnly = adminOnly instanceof Boolean b ? b : Integer.parseInt(String.valueOf(adminOnly)) != 0;
            if (isAdminOnly) {
                throw new BusinessException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "FORUM_422_TAG_NOT_ALLOWED",
                        "Tag is not allowed for normal user"
                );
            }
        }
    }

    private CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        String userUuid = String.valueOf(authentication.getPrincipal());
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String auth = authority.getAuthority();
            if (auth != null && auth.startsWith("ROLE_")) {
                roles.add(auth.substring(5));
            }
        }
        String role = roles.stream().findFirst().orElse("USER");
        return new CurrentUser(userUuid, role);
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ADMIN_403_ONLY_ADMIN", "Only admin can access");
        }
    }

    public record CreatePostCommand(String title, String content, List<Long> tagIds) {
    }

    public record UpdatePostCommand(String title, String content, List<Long> tagIds) {
    }

    public record PostView(Long id, String authorUuid, String title, String content, String reviewStatus, boolean pinned) {
    }

    private record CurrentUser(String userUuid, String role) {
    }
}

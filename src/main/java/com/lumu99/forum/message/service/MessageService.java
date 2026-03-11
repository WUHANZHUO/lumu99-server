package com.lumu99.forum.message.service;

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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class MessageService {

    private final JdbcTemplate jdbcTemplate;

    public MessageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ThreadView> listThreads() {
        String userUuid = currentUserUuid();
        return jdbcTemplate.query(
                "SELECT id, user_a_uuid, user_b_uuid, last_message_at, last_message_preview FROM dm_threads WHERE user_a_uuid = ? OR user_b_uuid = ? ORDER BY last_message_at DESC",
                (rs, rowNum) -> new ThreadView(
                        rs.getLong("id"),
                        rs.getString("user_a_uuid"),
                        rs.getString("user_b_uuid"),
                        rs.getTimestamp("last_message_at") == null ? null : rs.getTimestamp("last_message_at").toInstant(),
                        rs.getString("last_message_preview")
                ),
                userUuid, userUuid
        );
    }

    public List<MessageView> listThreadMessages(Long threadId) {
        String userUuid = currentUserUuid();
        ensureThreadOwnedByUser(threadId, userUuid);
        return jdbcTemplate.query(
                "SELECT id, thread_id, from_uuid, to_uuid, content, is_read, created_at FROM dm_messages WHERE thread_id = ? ORDER BY id ASC",
                (rs, rowNum) -> new MessageView(
                        rs.getLong("id"),
                        rs.getLong("thread_id"),
                        rs.getString("from_uuid"),
                        rs.getString("to_uuid"),
                        rs.getString("content"),
                        rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                threadId
        );
    }

    public MessageView sendMessage(String toUserUuid, String content) {
        String fromUserUuid = currentUserUuid();
        String fromRole = findUserRole(fromUserUuid);
        String toRole = findUserRole(toUserUuid);

        if ("USER".equals(fromRole) && "USER".equals(toRole) && !isUserDmEnabled()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "MSG_403_USER_DM_DISABLED", "User DM is disabled");
        }

        Long threadId = findOrCreateThread(fromUserUuid, toUserUuid);
        Instant now = Instant.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO dm_messages (thread_id, from_uuid, to_uuid, content, is_read, created_at) VALUES (?, ?, ?, ?, false, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, threadId);
            ps.setString(2, fromUserUuid);
            ps.setString(3, toUserUuid);
            ps.setString(4, content);
            ps.setTimestamp(5, Timestamp.from(now));
            return ps;
        }, keyHolder);

        jdbcTemplate.update(
                "UPDATE dm_threads SET last_message_at = ?, last_message_preview = ? WHERE id = ?",
                Timestamp.from(now),
                preview(content),
                threadId
        );

        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        if (id == null) {
            throw new IllegalStateException("Failed to create message");
        }
        return new MessageView(id, threadId, fromUserUuid, toUserUuid, content, false, now);
    }

    private Long findOrCreateThread(String userA, String userB) {
        Long existing = jdbcTemplate.query(
                "SELECT id FROM dm_threads WHERE (user_a_uuid = ? AND user_b_uuid = ?) OR (user_a_uuid = ? AND user_b_uuid = ?)",
                rs -> rs.next() ? rs.getLong("id") : null,
                userA, userB, userB, userA
        );
        if (existing != null) {
            return existing;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO dm_threads (user_a_uuid, user_b_uuid, last_message_at, last_message_preview) VALUES (?, ?, NULL, NULL)",
                    new String[]{"id"}
            );
            ps.setString(1, userA);
            ps.setString(2, userB);
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("Failed to create thread");
        }
        return keyHolder.getKey().longValue();
    }

    private void ensureThreadOwnedByUser(Long threadId, String userUuid) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dm_threads WHERE id = ? AND (user_a_uuid = ? OR user_b_uuid = ?)",
                Integer.class,
                threadId,
                userUuid,
                userUuid
        );
        if (count == null || count == 0) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_403_FORBIDDEN", "Forbidden");
        }
    }

    private String findUserRole(String userUuid) {
        String role = jdbcTemplate.query(
                "SELECT role FROM users WHERE user_uuid = ?",
                rs -> rs.next() ? rs.getString("role") : null,
                userUuid
        );
        if (role == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "User not found");
        }
        return role;
    }

    private boolean isUserDmEnabled() {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT user_dm_enabled FROM admin_settings WHERE id = 1",
                Boolean.class
        ));
    }

    private String currentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        return String.valueOf(authentication.getPrincipal());
    }

    private String preview(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= 64 ? content : content.substring(0, 64);
    }

    public record ThreadView(Long id,
                             String userAUuid,
                             String userBUuid,
                             Instant lastMessageAt,
                             String lastMessagePreview) {
    }

    public record MessageView(Long id,
                              Long threadId,
                              String fromUuid,
                              String toUuid,
                              String content,
                              boolean isRead,
                              Instant createdAt) {
    }
}

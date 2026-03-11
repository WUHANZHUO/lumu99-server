package com.lumu99.forum.invite.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class InviteCodeRepository {

    private final JdbcTemplate jdbcTemplate;

    public InviteCodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<InviteCodeRecord> findByCode(String code) {
        List<InviteCodeRecord> rows = jdbcTemplate.query(
                "SELECT id, code, status, expires_at, used_by_user_uuid, used_at FROM invite_codes WHERE code = ?",
                (rs, rowNum) -> new InviteCodeRecord(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("status"),
                        rs.getTimestamp("expires_at") == null ? null : rs.getTimestamp("expires_at").toInstant(),
                        rs.getString("used_by_user_uuid"),
                        rs.getTimestamp("used_at") == null ? null : rs.getTimestamp("used_at").toInstant()
                ),
                code
        );
        return rows.stream().findFirst();
    }

    public int markUsed(Long id, String userUuid, Instant usedAt) {
        return jdbcTemplate.update(
                "UPDATE invite_codes SET status = 'USED', used_by_user_uuid = ?, used_at = ? WHERE id = ? AND status = 'UNUSED'",
                userUuid,
                Timestamp.from(usedAt),
                id
        );
    }

    public InviteCodeRecord createInviteCode(String code, Instant expiresAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO invite_codes (code, status, expires_at) VALUES (?, 'UNUSED', ?)",
                    new String[]{"id"}
            );
            ps.setString(1, code);
            if (expiresAt == null) {
                ps.setTimestamp(2, null);
            } else {
                ps.setTimestamp(2, Timestamp.from(expiresAt));
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        if (id == null) {
            throw new IllegalStateException("Failed to create invite code");
        }
        return findByCode(code).orElseThrow(() -> new IllegalStateException("Created invite code not found"));
    }

    public List<InviteCodeRecord> listAll() {
        return jdbcTemplate.query(
                "SELECT id, code, status, expires_at, used_by_user_uuid, used_at FROM invite_codes ORDER BY id DESC",
                (rs, rowNum) -> new InviteCodeRecord(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("status"),
                        rs.getTimestamp("expires_at") == null ? null : rs.getTimestamp("expires_at").toInstant(),
                        rs.getString("used_by_user_uuid"),
                        rs.getTimestamp("used_at") == null ? null : rs.getTimestamp("used_at").toInstant()
                )
        );
    }

    public record InviteCodeRecord(
            Long id,
            String code,
            String status,
            Instant expiresAt,
            String usedByUserUuid,
            Instant usedAt
    ) {
    }
}

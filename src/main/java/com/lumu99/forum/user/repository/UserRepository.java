package com.lumu99.forum.user.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<UserRecord> userMapper = (rs, rowNum) -> new UserRecord(
            rs.getLong("id"),
            rs.getString("user_uuid"),
            rs.getString("username"),
            rs.getString("weibo_name"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getString("status"),
            rs.getString("mute_status")
    );

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserRecord> findByUsername(String username) {
        List<UserRecord> users = jdbcTemplate.query(
                "SELECT id,user_uuid,username,weibo_name,password_hash,role,status,mute_status FROM users WHERE username = ?",
                userMapper,
                username
        );
        return users.stream().findFirst();
    }

    public Optional<UserRecord> findByUserUuid(String userUuid) {
        List<UserRecord> users = jdbcTemplate.query(
                "SELECT id,user_uuid,username,weibo_name,password_hash,role,status,mute_status FROM users WHERE user_uuid = ?",
                userMapper,
                userUuid
        );
        return users.stream().findFirst();
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public int updateUsername(String userUuid, String username) {
        return jdbcTemplate.update(
                "UPDATE users SET username = ? WHERE user_uuid = ?",
                username,
                userUuid
        );
    }

    public int updatePasswordHash(String userUuid, String passwordHash) {
        return jdbcTemplate.update(
                "UPDATE users SET password_hash = ? WHERE user_uuid = ?",
                passwordHash,
                userUuid
        );
    }

    public int updateStatus(String userUuid, String status) {
        return jdbcTemplate.update(
                "UPDATE users SET status = ? WHERE user_uuid = ?",
                status,
                userUuid
        );
    }

    public record UserRecord(
            Long id,
            String userUuid,
            String username,
            String weiboName,
            String passwordHash,
            String role,
            String status,
            String muteStatus
    ) {
    }
}

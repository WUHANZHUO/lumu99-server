package com.lumu99.forum.user.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
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

    public boolean existsByWeiboName(String weiboName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE weibo_name = ?",
                Integer.class,
                weiboName
        );
        return count != null && count > 0;
    }

    public Long createUser(String userUuid,
                           String username,
                           String weiboName,
                           String passwordHash,
                           String role,
                           String status,
                           String muteStatus) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (user_uuid, username, weibo_name, password_hash, role, status, mute_status) VALUES (?,?,?,?,?,?,?)",
                    new String[]{"id"}
            );
            ps.setString(1, userUuid);
            ps.setString(2, username);
            ps.setString(3, weiboName);
            ps.setString(4, passwordHash);
            ps.setString(5, role);
            ps.setString(6, status);
            ps.setString(7, muteStatus);
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            return null;
        }
        return keyHolder.getKey().longValue();
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

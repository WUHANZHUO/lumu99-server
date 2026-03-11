package com.lumu99.forum.content.service;

import com.lumu99.forum.common.exception.BusinessException;
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
import java.util.List;

@Service
public class ContentService {

    private final JdbcTemplate jdbcTemplate;

    public ContentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ContentView> list(Module module) {
        boolean guest = isGuest();
        enforceGuestVisibility(module, guest);
        return jdbcTemplate.query(
                "SELECT id, title, body, resource_url, status, is_pinned FROM " + module.table + " ORDER BY is_pinned DESC, id DESC",
                (rs, rowNum) -> new ContentView(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getString("resource_url"),
                        rs.getString("status"),
                        rs.getBoolean("is_pinned")
                )
        );
    }

    public ContentView create(Module module, ContentCommand command) {
        requireAdmin();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + module.table + " (title, body, resource_url, status, is_pinned, created_by, updated_by) VALUES (?, ?, ?, ?, false, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, command.title());
            ps.setString(2, command.body());
            ps.setString(3, command.resourceUrl());
            ps.setString(4, command.status());
            ps.setString(5, currentUserUuid());
            ps.setString(6, currentUserUuid());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        if (id == null) {
            throw new IllegalStateException("Failed to create content");
        }
        return get(module, id);
    }

    public ContentView update(Module module, Long id, ContentCommand command) {
        requireAdmin();
        jdbcTemplate.update(
                "UPDATE " + module.table + " SET title=?, body=?, resource_url=?, status=?, updated_by=? WHERE id = ?",
                command.title(),
                command.body(),
                command.resourceUrl(),
                command.status(),
                currentUserUuid(),
                id
        );
        return get(module, id);
    }

    public void delete(Module module, Long id) {
        requireAdmin();
        jdbcTemplate.update("DELETE FROM " + module.table + " WHERE id = ?", id);
    }

    public ContentView pin(Module module, Long id, boolean pinned) {
        requireAdmin();
        jdbcTemplate.update("UPDATE " + module.table + " SET is_pinned = ?, updated_by = ? WHERE id = ?", pinned, currentUserUuid(), id);
        return get(module, id);
    }

    private ContentView get(Module module, Long id) {
        List<ContentView> list = jdbcTemplate.query(
                "SELECT id, title, body, resource_url, status, is_pinned FROM " + module.table + " WHERE id = ?",
                (rs, rowNum) -> new ContentView(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getString("resource_url"),
                        rs.getString("status"),
                        rs.getBoolean("is_pinned")
                ),
                id
        );
        if (list.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Content not found");
        }
        return list.get(0);
    }

    private void enforceGuestVisibility(Module module, boolean guest) {
        if (!guest) {
            return;
        }
        if (module == Module.WORLD) {
            Boolean visible = jdbcTemplate.queryForObject("SELECT world_guest_visible FROM admin_settings WHERE id = 1", Boolean.class);
            if (!Boolean.TRUE.equals(visible)) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_403_FORBIDDEN", "World is not visible for guest");
            }
        }
        if (module == Module.EVENT) {
            Boolean visible = jdbcTemplate.queryForObject("SELECT events_guest_visible FROM admin_settings WHERE id = 1", Boolean.class);
            if (!Boolean.TRUE.equals(visible)) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_403_FORBIDDEN", "Event is not visible for guest");
            }
        }
    }

    private boolean isGuest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken;
    }

    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ADMIN_403_ONLY_ADMIN", "Only admin can access");
        }
    }

    private String currentUserUuid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        return String.valueOf(auth.getPrincipal());
    }

    public record ContentCommand(String title, String body, String resourceUrl, String status) {
    }

    public record ContentView(Long id, String title, String body, String resourceUrl, String status, boolean pinned) {
    }

    public enum Module {
        STORY("content_story"),
        TIMELINE("content_timeline"),
        PHOTO("content_photo"),
        VIDEO("content_video"),
        WORLD("content_world"),
        EVENT("content_event");

        private final String table;

        Module(String table) {
            this.table = table;
        }
    }
}

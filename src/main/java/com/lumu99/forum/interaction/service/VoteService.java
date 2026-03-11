package com.lumu99.forum.interaction.service;

import com.lumu99.forum.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class VoteService {

    private final JdbcTemplate jdbcTemplate;

    public VoteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public VoteResult vote(TargetType targetType, Long targetId, VoteType voteType) {
        String userUuid = currentUserUuid();
        String existing = jdbcTemplate.query(
                "SELECT vote_type FROM votes WHERE target_type = ? AND target_id = ? AND user_uuid = ?",
                rs -> rs.next() ? rs.getString("vote_type") : null,
                targetType.name(),
                targetId,
                userUuid
        );

        if (existing == null) {
            jdbcTemplate.update(
                    "INSERT INTO votes (target_type, target_id, user_uuid, vote_type) VALUES (?, ?, ?, ?)",
                    targetType.name(),
                    targetId,
                    userUuid,
                    voteType.name()
            );
            return new VoteResult(voteType.name());
        }

        if (existing.equals(voteType.name())) {
            jdbcTemplate.update(
                    "DELETE FROM votes WHERE target_type = ? AND target_id = ? AND user_uuid = ?",
                    targetType.name(),
                    targetId,
                    userUuid
            );
            return new VoteResult("NONE");
        }

        jdbcTemplate.update(
                "UPDATE votes SET vote_type = ? WHERE target_type = ? AND target_id = ? AND user_uuid = ?",
                voteType.name(),
                targetType.name(),
                targetId,
                userUuid
        );
        return new VoteResult(voteType.name());
    }

    private String currentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        return String.valueOf(authentication.getPrincipal());
    }

    public enum VoteType {
        LIKE, DISLIKE
    }

    public enum TargetType {
        FORUM_POST, PHOTO, VIDEO, WORLD
    }

    public record VoteResult(String currentVote) {
    }
}

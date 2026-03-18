package com.lumu99.forum.dto.response;

import com.lumu99.forum.domain.ForumPost;

public record ReviewPostResponse(
        Long id,
        String authorUuid,
        String title,
        String content,
        String reviewStatus,
        String rejectReason
) {
    public static ReviewPostResponse from(ForumPost post) {
        return new ReviewPostResponse(
                post.getId(),
                post.getAuthorUuid(),
                post.getTitle(),
                post.getContent(),
                post.getReviewStatus() != null ? post.getReviewStatus().name() : null,
                post.getRejectReason()
        );
    }
}

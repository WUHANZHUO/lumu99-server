package com.lumu99.forum.dto.response;

import com.lumu99.forum.domain.ForumComment;

public record CommentResponse(
        Long id,
        Long postId,
        String authorUuid,
        String content
) {
    public static CommentResponse from(ForumComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getAuthorUuid(),
                comment.getContent()
        );
    }
}

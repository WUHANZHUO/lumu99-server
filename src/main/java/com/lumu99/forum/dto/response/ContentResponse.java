package com.lumu99.forum.dto.response;

import com.lumu99.forum.domain.content.BaseContent;

public record ContentResponse(
        Long id,
        String title,
        String body,
        String resourceUrl,
        String status,
        boolean pinned
) {
    public static ContentResponse from(BaseContent c) {
        return new ContentResponse(
                c.getId(),
                c.getTitle(),
                c.getBody(),
                c.getResourceUrl(),
                c.getStatus() != null ? c.getStatus().name() : null,
                Boolean.TRUE.equals(c.getIsPinned())
        );
    }
}

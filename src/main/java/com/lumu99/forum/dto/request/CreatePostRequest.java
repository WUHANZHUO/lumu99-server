package com.lumu99.forum.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        List<Long> tagIds
) {}

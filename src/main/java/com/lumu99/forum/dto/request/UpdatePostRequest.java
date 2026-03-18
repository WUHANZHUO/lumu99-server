package com.lumu99.forum.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        List<Long> tagIds
) {}

package com.lumu99.forum.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ContentRequest(
        @NotBlank String title,
        String body,
        String resourceUrl,
        String status
) {}

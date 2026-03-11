package com.lumu99.forum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lumuForumOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Lumu99 Forum Backend API")
                .version("v1")
                .description("Backend API for auth, forum, moderation, content, interaction and messaging."));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/auth/**", "/users/me/**")
                .addOpenApiCustomizer(authCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi forumApi() {
        return GroupedOpenApi.builder()
                .group("forum")
                .pathsToMatch("/forum/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reviewApi() {
        return GroupedOpenApi.builder()
                .group("review")
                .pathsToMatch("/admin/reviews/**")
                .build();
    }

    @Bean
    public GroupedOpenApi contentApi() {
        return GroupedOpenApi.builder()
                .group("content")
                .pathsToMatch("/stories/**", "/timelines/**", "/photos/**", "/videos/**", "/worlds/**", "/events/**")
                .build();
    }

    @Bean
    public GroupedOpenApi messageApi() {
        return GroupedOpenApi.builder()
                .group("message")
                .pathsToMatch("/messages/**")
                .build();
    }

    private OpenApiCustomizer authCustomizer() {
        return openApi -> {
            setSummary(openApi, "/auth/login", "User login with username and password");
            setSummary(openApi, "/auth/logout", "User logout (stateless)");
            setSummary(openApi, "/users/me/username", "Change current username");
            setSummary(openApi, "/users/me/password", "Change current password");
            setSummary(openApi, "/users/me", "Deactivate current account");
            addLoginExamples(openApi);
        };
    }

    private void setSummary(OpenAPI openApi, String path, String summary) {
        if (openApi.getPaths() == null) {
            return;
        }
        PathItem pathItem = openApi.getPaths().get(path);
        if (pathItem == null) {
            return;
        }
        for (Operation operation : pathItem.readOperations()) {
            if (operation.getSummary() == null || operation.getSummary().isBlank()) {
                operation.setSummary(summary);
            }
        }
    }

    private void addLoginExamples(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }
        PathItem pathItem = openApi.getPaths().get("/auth/login");
        if (pathItem == null || pathItem.getPost() == null) {
            return;
        }
        Operation operation = pathItem.getPost();
        if (operation.getRequestBody() != null
                && operation.getRequestBody().getContent() != null
                && operation.getRequestBody().getContent().get("application/json") != null) {
            operation.getRequestBody().getContent().get("application/json")
                    .setExample(Map.of("username", "demo_user", "password", "******"));
        }
        if (operation.getResponses() != null
                && operation.getResponses().get("200") != null
                && operation.getResponses().get("200").getContent() != null
                && operation.getResponses().get("200").getContent().get("application/json") != null) {
            operation.getResponses().get("200").getContent().get("application/json")
                    .setExample(Map.of(
                            "data", Map.of(
                                    "token", "<jwt>",
                                    "userUuid", "00000000-0000-0000-0000-000000000001",
                                    "role", "USER"
                            )
                    ));
        }
    }
}

package com.lumu99.forum.forum.controller;

import com.lumu99.forum.forum.service.ForumCommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/forum/posts/{postId}/comments")
public class ForumCommentController {

    private final ForumCommentService forumCommentService;

    public ForumCommentController(ForumCommentService forumCommentService) {
        this.forumCommentService = forumCommentService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listComments(@PathVariable Long postId) {
        return ResponseEntity.ok(Map.of("data", forumCommentService.listComments(postId)));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createComment(@PathVariable Long postId,
                                                             @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("data", forumCommentService.createComment(postId, request.content())));
    }

    public record CommentRequest(@NotBlank String content) {
    }
}

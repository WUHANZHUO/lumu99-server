package com.lumu99.forum.forum.controller;

import com.lumu99.forum.dto.request.CreatePostRequest;
import com.lumu99.forum.dto.request.UpdatePostRequest;
import com.lumu99.forum.dto.response.PostResponse;
import com.lumu99.forum.forum.service.ForumPostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forum/posts")
public class ForumPostController {

    private final ForumPostService forumPostService;

    public ForumPostController(ForumPostService forumPostService) {
        this.forumPostService = forumPostService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPosts() {
        List<PostResponse> posts = forumPostService.listPosts();
        return ResponseEntity.ok(Map.of("data", posts));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPost(@Valid @RequestBody CreatePostRequest request) {
        PostResponse post = forumPostService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", post));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getPost(@PathVariable Long postId) {
        PostResponse post = forumPostService.getPost(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return ResponseEntity.ok(Map.of("data", post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> updatePost(@PathVariable Long postId,
                                                           @Valid @RequestBody UpdatePostRequest request) {
        PostResponse post = forumPostService.updatePost(postId, request);
        return ResponseEntity.ok(Map.of("data", post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long postId) {
        forumPostService.deletePost(postId);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @PostMapping("/{postId}/pin")
    public ResponseEntity<Map<String, Object>> pinPost(@PathVariable Long postId) {
        return ResponseEntity.ok(Map.of("data", forumPostService.pinPost(postId)));
    }

    @DeleteMapping("/{postId}/pin")
    public ResponseEntity<Map<String, Object>> unpinPost(@PathVariable Long postId) {
        return ResponseEntity.ok(Map.of("data", forumPostService.unpinPost(postId)));
    }
}

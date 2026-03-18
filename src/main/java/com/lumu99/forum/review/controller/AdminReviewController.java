package com.lumu99.forum.review.controller;

import com.lumu99.forum.dto.response.ReviewPostResponse;
import com.lumu99.forum.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/reviews/posts")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPendingPosts() {
        return ResponseEntity.ok(Map.of("data", reviewService.listPendingPosts()));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getPost(@PathVariable Long postId) {
        ReviewPostResponse post = reviewService.getPost(postId).orElse(null);
        return ResponseEntity.ok(Map.of("data", post != null ? post : Map.of()));
    }

    @PostMapping("/{postId}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long postId) {
        return ResponseEntity.ok(Map.of("data", reviewService.approve(postId)));
    }

    @PostMapping("/{postId}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long postId,
                                                      @RequestBody(required = false) RejectRequest request) {
        String reason = request == null ? null : request.reason();
        return ResponseEntity.ok(Map.of("data", reviewService.reject(postId, reason)));
    }

    public record RejectRequest(String reason) {}
}

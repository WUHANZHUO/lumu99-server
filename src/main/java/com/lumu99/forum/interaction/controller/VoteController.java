package com.lumu99.forum.interaction.controller;

import com.lumu99.forum.common.enums.VoteTargetType;
import com.lumu99.forum.common.enums.VoteType;
import com.lumu99.forum.interaction.service.VoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/forum/posts/{id}/vote")
    public ResponseEntity<Map<String, Object>> voteForumPost(@PathVariable Long id, @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(Map.of("data", voteService.vote(VoteTargetType.FORUM_POST, id, request.voteType())));
    }

    @PostMapping("/photos/{id}/vote")
    public ResponseEntity<Map<String, Object>> votePhoto(@PathVariable Long id, @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(Map.of("data", voteService.vote(VoteTargetType.PHOTO, id, request.voteType())));
    }

    @PostMapping("/videos/{id}/vote")
    public ResponseEntity<Map<String, Object>> voteVideo(@PathVariable Long id, @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(Map.of("data", voteService.vote(VoteTargetType.VIDEO, id, request.voteType())));
    }

    @PostMapping("/worlds/{id}/vote")
    public ResponseEntity<Map<String, Object>> voteWorld(@PathVariable Long id, @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(Map.of("data", voteService.vote(VoteTargetType.WORLD, id, request.voteType())));
    }

    public record VoteRequest(@NotNull VoteType voteType) {}
}

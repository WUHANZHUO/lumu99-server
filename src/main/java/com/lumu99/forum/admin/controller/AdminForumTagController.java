package com.lumu99.forum.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lumu99.forum.domain.ForumTag;
import com.lumu99.forum.mapper.ForumTagMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/admin/forum-tags")
public class AdminForumTagController {

    private final ForumTagMapper forumTagMapper;

    public AdminForumTagController(ForumTagMapper forumTagMapper) {
        this.forumTagMapper = forumTagMapper;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<ForumTag> tags = forumTagMapper.selectList(
                new LambdaQueryWrapper<ForumTag>().orderByDesc(ForumTag::getId)
        );
        return ResponseEntity.ok(Map.of("data", tags));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ForumTagRequest request) {
        ForumTag tag = new ForumTag();
        tag.setName(request.name());
        tag.setAdminOnly(request.adminOnly());
        tag.setEnabled(request.enabled());
        forumTagMapper.insert(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                      @Valid @RequestBody ForumTagRequest request) {
        forumTagMapper.update(new LambdaUpdateWrapper<ForumTag>()
                .eq(ForumTag::getId, id)
                .set(ForumTag::getName, request.name())
                .set(ForumTag::getAdminOnly, request.adminOnly())
                .set(ForumTag::getEnabled, request.enabled()));
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        forumTagMapper.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    public record ForumTagRequest(
            @NotBlank String name,
            @NotNull Boolean adminOnly,
            @NotNull Boolean enabled
    ) {}
}

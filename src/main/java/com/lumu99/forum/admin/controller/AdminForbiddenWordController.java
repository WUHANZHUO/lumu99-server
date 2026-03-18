package com.lumu99.forum.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lumu99.forum.domain.ForbiddenWord;
import com.lumu99.forum.mapper.ForbiddenWordMapper;
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
@RequestMapping("/admin/forbidden-words")
public class AdminForbiddenWordController {

    private final ForbiddenWordMapper forbiddenWordMapper;

    public AdminForbiddenWordController(ForbiddenWordMapper forbiddenWordMapper) {
        this.forbiddenWordMapper = forbiddenWordMapper;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<ForbiddenWord> words = forbiddenWordMapper.selectList(
                new LambdaQueryWrapper<ForbiddenWord>().orderByDesc(ForbiddenWord::getId)
        );
        return ResponseEntity.ok(Map.of("data", words));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ForbiddenWordRequest request) {
        ForbiddenWord word = new ForbiddenWord();
        word.setWord(request.word());
        word.setEnabled(request.enabled());
        forbiddenWordMapper.insert(word);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                      @Valid @RequestBody ForbiddenWordRequest request) {
        forbiddenWordMapper.update(new LambdaUpdateWrapper<ForbiddenWord>()
                .eq(ForbiddenWord::getId, id)
                .set(ForbiddenWord::getWord, request.word())
                .set(ForbiddenWord::getEnabled, request.enabled()));
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        forbiddenWordMapper.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    public record ForbiddenWordRequest(
            @NotBlank String word,
            @NotNull Boolean enabled
    ) {}
}

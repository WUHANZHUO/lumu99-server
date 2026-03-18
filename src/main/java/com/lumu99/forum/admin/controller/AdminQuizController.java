package com.lumu99.forum.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lumu99.forum.domain.QuizConfig;
import com.lumu99.forum.domain.QuizOption;
import com.lumu99.forum.domain.QuizQuestion;
import com.lumu99.forum.mapper.QuizConfigMapper;
import com.lumu99.forum.mapper.QuizOptionMapper;
import com.lumu99.forum.mapper.QuizQuestionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/admin/quiz")
public class AdminQuizController {

    private final QuizQuestionMapper quizQuestionMapper;
    private final QuizOptionMapper quizOptionMapper;
    private final QuizConfigMapper quizConfigMapper;

    public AdminQuizController(QuizQuestionMapper quizQuestionMapper,
                               QuizOptionMapper quizOptionMapper,
                               QuizConfigMapper quizConfigMapper) {
        this.quizQuestionMapper = quizQuestionMapper;
        this.quizOptionMapper = quizOptionMapper;
        this.quizConfigMapper = quizConfigMapper;
    }

    @GetMapping("/questions")
    public ResponseEntity<Map<String, Object>> listQuestions() {
        List<QuizQuestion> questions = quizQuestionMapper.selectList(
                new LambdaQueryWrapper<QuizQuestion>().orderByDesc(QuizQuestion::getId)
        );
        return ResponseEntity.ok(Map.of("data", questions));
    }

    @PostMapping("/questions")
    public ResponseEntity<Map<String, Object>> createQuestion(@Valid @RequestBody QuizQuestionRequest request) {
        QuizQuestion question = new QuizQuestion();
        question.setQuestionType(request.questionType());
        question.setStem(request.stem());
        question.setAnswerText(request.answerText());
        question.setEnabled(request.enabled());
        quizQuestionMapper.insert(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "OK"));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<Map<String, Object>> updateQuestion(@PathVariable Long id,
                                                              @Valid @RequestBody QuizQuestionRequest request) {
        quizQuestionMapper.update(new LambdaUpdateWrapper<QuizQuestion>()
                .eq(QuizQuestion::getId, id)
                .set(QuizQuestion::getQuestionType, request.questionType())
                .set(QuizQuestion::getStem, request.stem())
                .set(QuizQuestion::getAnswerText, request.answerText())
                .set(QuizQuestion::getEnabled, request.enabled()));
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @Transactional
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(@PathVariable Long id) {
        quizOptionMapper.delete(new LambdaQueryWrapper<QuizOption>().eq(QuizOption::getQuestionId, id));
        quizQuestionMapper.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@Valid @RequestBody QuizConfigRequest request) {
        quizConfigMapper.update(new LambdaUpdateWrapper<QuizConfig>()
                .eq(QuizConfig::getId, 1L)
                .set(QuizConfig::getQuestionCount, request.questionCount())
                .set(QuizConfig::getPassScore, request.passScore()));
        return ResponseEntity.ok(Map.of("data", request));
    }

    public record QuizQuestionRequest(
            @NotBlank String questionType,
            @NotBlank String stem,
            @NotBlank String answerText,
            @NotNull Boolean enabled
    ) {}

    public record QuizConfigRequest(
            @NotNull Integer questionCount,
            @NotNull Integer passScore
    ) {}
}

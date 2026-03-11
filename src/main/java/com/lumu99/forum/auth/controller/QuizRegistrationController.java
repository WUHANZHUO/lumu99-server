package com.lumu99.forum.auth.controller;

import com.lumu99.forum.quiz.service.QuizService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth/register/quiz")
public class QuizRegistrationController {

    private final QuizService quizService;

    public QuizRegistrationController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start() {
        return ResponseEntity.ok(Map.of("data", quizService.startQuiz()));
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody QuizSubmitRequest request) {
        QuizService.RegistrationResult result = quizService.submitQuiz(new QuizService.SubmitQuizCommand(
                request.username(),
                request.password(),
                request.weiboName(),
                request.answers().stream()
                        .map(a -> new QuizService.AnswerCommand(a.questionId(), a.answer()))
                        .toList()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", result));
    }

    public record QuizSubmitRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String weiboName,
            @NotNull List<QuizAnswerRequest> answers
    ) {
    }

    public record QuizAnswerRequest(
            @NotNull Long questionId,
            @NotBlank String answer
    ) {
    }
}

package com.lumu99.forum.quiz.service;

import com.lumu99.forum.auth.security.JwtService;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.quiz.repository.QuizRepository;
import com.lumu99.forum.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public QuizService(QuizRepository quizRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public StartQuizResult startQuiz() {
        int questionCount = quizRepository.currentQuestionCount();
        List<QuestionView> questions = quizRepository.randomEnabledQuestions(questionCount).stream()
                .map(question -> new QuestionView(
                        question.id(),
                        question.questionType(),
                        question.stem(),
                        quizRepository.findOptionsByQuestionId(question.id()).stream()
                                .map(option -> new OptionView(option.optionKey(), option.optionText()))
                                .toList()
                ))
                .toList();
        return new StartQuizResult(questions);
    }

    public RegistrationResult submitQuiz(SubmitQuizCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_USERNAME_EXISTS", "Username already exists");
        }
        if (userRepository.existsByWeiboName(command.weiboName())) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_WEIBO_EXISTS", "Weibo name already exists");
        }
        verifyAnswers(command.answers());

        String userUuid = UUID.randomUUID().toString();
        String passwordHash = passwordEncoder.encode(command.password());
        userRepository.createUser(userUuid, command.username(), command.weiboName(), passwordHash, "USER", "ACTIVE", "NORMAL");
        String token = jwtService.generateToken(userUuid, "USER");
        return new RegistrationResult(userUuid, token);
    }

    private void verifyAnswers(List<AnswerCommand> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "REG_422_QUIZ_ANSWER_WRONG",
                    "Quiz answer is wrong"
            );
        }

        for (AnswerCommand answer : answers) {
            QuizRepository.QuizQuestion question = quizRepository.findEnabledQuestionById(answer.questionId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "REG_422_QUIZ_ANSWER_WRONG",
                            "Quiz answer is wrong"
                    ));
            if (!StringUtils.hasText(answer.answer())) {
                throw new BusinessException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "REG_422_QUIZ_ANSWER_WRONG",
                        "Quiz answer is wrong"
                );
            }

            if ("FILL_BLANK".equals(question.questionType())) {
                if (!question.answerText().equals(answer.answer())) {
                    throw new BusinessException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "REG_422_QUIZ_ANSWER_WRONG",
                            "Quiz answer is wrong"
                    );
                }
                continue;
            }

            if ("SINGLE_CHOICE".equals(question.questionType())) {
                if (!question.answerText().equals(answer.answer())) {
                    throw new BusinessException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "REG_422_QUIZ_ANSWER_WRONG",
                            "Quiz answer is wrong"
                    );
                }
                continue;
            }

            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "REG_422_QUIZ_ANSWER_WRONG",
                    "Quiz answer is wrong"
            );
        }
    }

    public record StartQuizResult(List<QuestionView> questions) {
    }

    public record QuestionView(Long questionId, String questionType, String stem, List<OptionView> options) {
    }

    public record OptionView(String optionKey, String optionText) {
    }

    public record SubmitQuizCommand(String username,
                                    String password,
                                    String weiboName,
                                    List<AnswerCommand> answers) {
    }

    public record AnswerCommand(Long questionId, String answer) {
    }

    public record RegistrationResult(String userUuid, String token) {
    }
}

package com.lumu99.forum.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.auth.security.JwtService;
import com.lumu99.forum.common.enums.MuteStatus;
import com.lumu99.forum.common.enums.UserRole;
import com.lumu99.forum.common.enums.UserStatus;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.domain.QuizConfig;
import com.lumu99.forum.domain.QuizOption;
import com.lumu99.forum.domain.QuizQuestion;
import com.lumu99.forum.domain.User;
import com.lumu99.forum.mapper.QuizConfigMapper;
import com.lumu99.forum.mapper.QuizOptionMapper;
import com.lumu99.forum.mapper.QuizQuestionMapper;
import com.lumu99.forum.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class QuizService {

    private final QuizQuestionMapper quizQuestionMapper;
    private final QuizOptionMapper quizOptionMapper;
    private final QuizConfigMapper quizConfigMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public QuizService(QuizQuestionMapper quizQuestionMapper,
                       QuizOptionMapper quizOptionMapper,
                       QuizConfigMapper quizConfigMapper,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.quizQuestionMapper = quizQuestionMapper;
        this.quizOptionMapper = quizOptionMapper;
        this.quizConfigMapper = quizConfigMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public StartQuizResult startQuiz() {
        QuizConfig config = quizConfigMapper.selectById(1L);
        int questionCount = config != null ? config.getQuestionCount() : 3;

        List<QuizQuestion> questions = quizQuestionMapper.selectList(
                new LambdaQueryWrapper<QuizQuestion>()
                        .eq(QuizQuestion::getEnabled, true)
                        .last("ORDER BY RAND() LIMIT " + questionCount)
        );

        List<QuestionView> views = questions.stream().map(q -> {
            List<QuizOption> options = quizOptionMapper.selectList(
                    new LambdaQueryWrapper<QuizOption>().eq(QuizOption::getQuestionId, q.getId())
            );
            List<OptionView> optionViews = options.stream()
                    .map(o -> new OptionView(o.getOptionKey(), o.getOptionText()))
                    .toList();
            return new QuestionView(q.getId(), q.getQuestionType(), q.getStem(), optionViews);
        }).toList();

        return new StartQuizResult(views);
    }

    public RegistrationResult submitQuiz(SubmitQuizCommand command) {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, command.username())) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_USERNAME_EXISTS", "Username already exists");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getWeiboName, command.weiboName())) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "REG_409_WEIBO_EXISTS", "Weibo name already exists");
        }
        verifyAnswers(command.answers());

        String userUuid = UUID.randomUUID().toString();
        User user = new User();
        user.setUserUuid(userUuid);
        user.setUsername(command.username());
        user.setWeiboName(command.weiboName());
        user.setPasswordHash(passwordEncoder.encode(command.password()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setMuteStatus(MuteStatus.NORMAL);
        userMapper.insert(user);

        String token = jwtService.generateToken(userUuid, UserRole.USER.name());
        return new RegistrationResult(userUuid, token);
    }

    private void verifyAnswers(List<AnswerCommand> answers) {
        if (answers == null || answers.isEmpty()) {
            throw wrongAnswer();
        }
        for (AnswerCommand answer : answers) {
            QuizQuestion question = quizQuestionMapper.selectOne(
                    new LambdaQueryWrapper<QuizQuestion>()
                            .eq(QuizQuestion::getId, answer.questionId())
                            .eq(QuizQuestion::getEnabled, true)
            );
            if (question == null || !StringUtils.hasText(answer.answer())) {
                throw wrongAnswer();
            }
            if (!question.getAnswerText().equals(answer.answer())) {
                throw wrongAnswer();
            }
        }
    }

    private BusinessException wrongAnswer() {
        return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "REG_422_QUIZ_ANSWER_WRONG", "Quiz answer is wrong");
    }

    public record StartQuizResult(List<QuestionView> questions) {}
    public record QuestionView(Long questionId, String questionType, String stem, List<OptionView> options) {}
    public record OptionView(String optionKey, String optionText) {}
    public record SubmitQuizCommand(String username, String password, String weiboName, List<AnswerCommand> answers) {}
    public record AnswerCommand(Long questionId, String answer) {}
    public record RegistrationResult(String userUuid, String token) {}
}

package com.lumu99.forum.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizRegistrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM quiz_options");
        jdbc.update("DELETE FROM quiz_questions");
        jdbc.update("UPDATE quiz_config SET question_count = 1 WHERE id = 1");
        jdbc.update(
                "INSERT INTO quiz_questions (id, question_type, stem, answer_text, enabled) VALUES (?,?,?,?,?)",
                1001L,
                "FILL_BLANK",
                "Who is this forum for?",
                "Lumu99",
                true
        );
    }

    @Test
    void fillBlankMustMatchExactly() throws Exception {
        mvc.perform(post("/auth/register/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"quiz_user",
                                  "password":"Secret123",
                                  "weiboName":"quiz_weibo",
                                  "answers":[{"questionId":1001,"answer":"lumu99"}]
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("REG_422_QUIZ_ANSWER_WRONG"));
    }
}

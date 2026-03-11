package com.lumu99.forum.invite;

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
class InviteCodeFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM invite_codes");
        jdbc.update("INSERT INTO invite_codes (code, status) VALUES (?, ?)", "CODE-1", "UNUSED");
    }

    @Test
    void usedInviteCodeCannotBeReused() throws Exception {
        mvc.perform(post("/auth/register/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"invite_user_1",
                                  "password":"Secret123",
                                  "weiboName":"invite_weibo_1",
                                  "inviteCode":"CODE-1"
                                }
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/auth/register/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"invite_user_2",
                                  "password":"Secret123",
                                  "weiboName":"invite_weibo_2",
                                  "inviteCode":"CODE-1"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("REG_422_INVITE_CODE_INVALID"));
    }
}

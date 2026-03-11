package com.lumu99.forum.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthLifecycleTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM users");
        jdbc.update(
                "INSERT INTO users (user_uuid, username, weibo_name, password_hash, role, status, mute_status) VALUES (?,?,?,?,?,?,?)",
                UUID.randomUUID().toString(),
                "u1",
                "wb1",
                passwordEncoder.encode("p1"),
                "USER",
                "BANNED",
                "NORMAL"
        );
    }

    @Test
    void bannedUserCannotLogin() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u1\",\"password\":\"p1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("LOGIN_403_ACCOUNT_BANNED"));
    }
}

package com.lumu99.forum.message;

import com.lumu99.forum.auth.security.JwtService;
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
class MessagePermissionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    private String otherUserUuid;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM dm_messages");
        jdbc.update("DELETE FROM dm_threads");
        jdbc.update("DELETE FROM users");
        jdbc.update("UPDATE admin_settings SET user_dm_enabled = false WHERE id = 1");

        jdbc.update(
                "INSERT INTO users (user_uuid, username, weibo_name, password_hash, role, status, mute_status) VALUES (?,?,?,?,?,?,?)",
                "msg-user-1", "msg_user_1", "msg_wb_1", "x", "USER", "ACTIVE", "NORMAL"
        );
        otherUserUuid = "msg-user-2";
        jdbc.update(
                "INSERT INTO users (user_uuid, username, weibo_name, password_hash, role, status, mute_status) VALUES (?,?,?,?,?,?,?)",
                otherUserUuid, "msg_user_2", "msg_wb_2", "x", "USER", "ACTIVE", "NORMAL"
        );
    }

    @Test
    void userToUserMessageBlockedWhenSwitchOff() throws Exception {
        String userToken = "Bearer " + jwtService.generateToken("msg-user-1", "USER");
        mvc.perform(post("/messages/to/{uuid}", otherUserUuid)
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MSG_403_USER_DM_DISABLED"));
    }
}

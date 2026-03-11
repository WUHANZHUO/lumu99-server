package com.lumu99.forum.forum;

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
class ForumCommentMuteTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    private Long approvedPostId;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM forum_comments");
        jdbc.update("DELETE FROM forum_posts");
        jdbc.update("DELETE FROM users");
        jdbc.update(
                "INSERT INTO forum_posts (author_uuid, title, content, review_status, is_pinned, published_at) VALUES (?,?,?,?,?,NOW())",
                "author-1",
                "approved",
                "approved body",
                "APPROVED",
                false
        );
        approvedPostId = jdbc.queryForObject("SELECT MAX(id) FROM forum_posts", Long.class);
        jdbc.update(
                "INSERT INTO users (user_uuid, username, weibo_name, password_hash, role, status, mute_status) VALUES (?,?,?,?,?,?,?)",
                "muted-user-1",
                "muted_u1",
                "muted_wb1",
                "x",
                "USER",
                "ACTIVE",
                "MUTED"
        );
    }

    @Test
    void mutedUserCannotComment() throws Exception {
        String mutedUserToken = "Bearer " + jwtService.generateToken("muted-user-1", "USER");
        mvc.perform(post("/forum/posts/{id}/comments", approvedPostId)
                        .header("Authorization", mutedUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORUM_403_MUTED"));
    }
}

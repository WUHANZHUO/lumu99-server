package com.lumu99.forum.review;

import com.lumu99.forum.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminReviewFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    private Long pendingPostId;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM forum_posts");
        jdbc.update(
                "INSERT INTO forum_posts (author_uuid, title, content, review_status, is_pinned) VALUES (?,?,?,?,?)",
                "user-r1",
                "need review",
                "pending body",
                "PENDING",
                false
        );
        pendingPostId = jdbc.queryForObject("SELECT MAX(id) FROM forum_posts", Long.class);
    }

    @Test
    void adminCanApprovePendingPost() throws Exception {
        String adminToken = "Bearer " + jwtService.generateToken("admin-review-1", "ADMIN");
        mvc.perform(post("/admin/reviews/posts/{id}/approve", pendingPostId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"));
    }
}

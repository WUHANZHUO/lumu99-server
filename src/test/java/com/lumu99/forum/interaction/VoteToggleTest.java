package com.lumu99.forum.interaction;

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
class VoteToggleTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    private Long postId;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM votes");
        jdbc.update("DELETE FROM forum_posts");
        jdbc.update(
                "INSERT INTO forum_posts (author_uuid, title, content, review_status, is_pinned, published_at) VALUES (?,?,?,?,?,NOW())",
                "author-vote-1",
                "vote post",
                "vote body",
                "APPROVED",
                false
        );
        postId = jdbc.queryForObject("SELECT MAX(id) FROM forum_posts", Long.class);
    }

    @Test
    void likeAndDislikeMustBeMutuallyExclusive() throws Exception {
        String userToken = "Bearer " + jwtService.generateToken("vote-user-1", "USER");

        mvc.perform(post("/forum/posts/{id}/vote", postId)
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"voteType\":\"LIKE\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/forum/posts/{id}/vote", postId)
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"voteType\":\"DISLIKE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentVote").value("DISLIKE"));
    }
}

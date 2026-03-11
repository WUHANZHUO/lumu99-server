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
class ForumPostReviewTriggerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM forum_post_tag_rel");
        jdbc.update("DELETE FROM forum_posts");
        jdbc.update("DELETE FROM forbidden_words");
        jdbc.update("INSERT INTO forbidden_words (word, enabled) VALUES (?, ?)", "contains_word", true);
    }

    @Test
    void forbiddenWordPostMustBePending() throws Exception {
        String userToken = "Bearer " + jwtService.generateToken("user-post-1", "USER");
        mvc.perform(post("/forum/posts")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"hello",
                                  "content":"this post contains_word",
                                  "tagIds":[]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reviewStatus").value("PENDING"));
    }
}

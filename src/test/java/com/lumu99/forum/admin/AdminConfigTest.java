package com.lumu99.forum.admin;

import com.lumu99.forum.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminConfigTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void nonAdminCannotUpdateSettings() throws Exception {
        String userToken = "Bearer " + jwtService.generateToken("user-uuid-1", "USER");
        mvc.perform(put("/admin/settings")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "worldGuestVisible": true,
                                  "eventsGuestVisible": true,
                                  "forumPostNeedReview": true,
                                  "userDmEnabled": false
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ADMIN_403_ONLY_ADMIN"));
    }
}

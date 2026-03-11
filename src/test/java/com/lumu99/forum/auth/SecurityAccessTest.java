package com.lumu99.forum.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void guestCannotAccessAdminPath() throws Exception {
        mvc.perform(get("/admin/settings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authPathShouldNotRequireAuthentication() throws Exception {
        mvc.perform(get("/auth/login"))
                .andExpect(status().isNotFound());
    }
}

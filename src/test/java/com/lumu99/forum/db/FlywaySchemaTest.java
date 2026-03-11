package com.lumu99.forum.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.enabled=true")
@ActiveProfiles("test")
class FlywaySchemaTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldCreateCoreTables() {
        Integer count = jdbc.queryForObject(
                "select count(*) from information_schema.tables where table_schema = database() and table_name in ('users','forum_posts','audit_logs')",
                Integer.class
        );
        assertThat(count).isGreaterThanOrEqualTo(3);
    }
}

package com.org.gigscore.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true"
})
class SchemaValidationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void hibernateValidationPassesAgainstMigratedSchema() throws SQLException {
        assertThat(dataSource).isNotNull();

        Set<String> tables = new HashSet<>();
        try (var conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
        }

        assertThat(tables)
                .as("Flyway migration must create all entity tables and flyway history")
                .contains(
                        "flyway_schema_history",
                        "user",
                        "gig_data",
                        "gig_score",
                        "score_history",
                        "activity"
                );
    }
}

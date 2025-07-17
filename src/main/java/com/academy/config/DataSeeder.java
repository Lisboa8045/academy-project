package com.academy.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final DataSource dataSource;

    public DataSeeder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        Resource globalConfig = new ClassPathResource("sql-scripts/populateGlobalConfigurations.sql");
        Resource roles = new ClassPathResource("sql-scripts/populateRoles.sql");
        Resource serviceTypes = new ClassPathResource("sql-scripts/populateServiceTypes.sql");
        Resource defaultTags = new ClassPathResource("sql-scripts/populateDefaultTags.sql");
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, globalConfig);
            ScriptUtils.executeSqlScript(conn, roles);
            ScriptUtils.executeSqlScript(conn, serviceTypes);
            ScriptUtils.executeSqlScript(conn, defaultTags);
            System.out.println("Populated database");
        }
    }
}


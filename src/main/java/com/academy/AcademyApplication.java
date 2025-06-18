package com.academy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication
public class AcademyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcademyApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty(name = "starting.scripts.enabled", havingValue = "true", matchIfMissing = false)
	CommandLineRunner populateData(DataSource dataSource) {
		return args -> {
			Resource resource2= new ClassPathResource("sql-scripts/populateAvailabilities.sql");
			Resource resource3= new ClassPathResource("sql-scripts/populateServiceProviders.sql");
			Resource resource4= new ClassPathResource("sql-scripts/populateServices.sql");
			Resource resource5= new ClassPathResource("sql-scripts/populateAvailabilities.sql");
			try (Connection conn = dataSource.getConnection()) {
				ScriptUtils.executeSqlScript(conn, resource2);
				ScriptUtils.executeSqlScript(conn, resource3);
				ScriptUtils.executeSqlScript(conn, resource4);
				ScriptUtils.executeSqlScript(conn, resource5);
			}
			System.out.println("Populated database");
		};
	}



}

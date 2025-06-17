package com.academy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication
public class AcademyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcademyApplication.class, args);
	}

/*
	@Bean
	@ConditionalOnProperty(name = "starting.scripts.enabled", havingValue = "true", matchIfMissing = false)
	CommandLineRunner populateData(DataSource dataSource) {
		return args -> {
			Resource resource4 = new ClassPathResource("sql-scripts/populateGlobalConfigurations.sql");
			Resource resource5 = new ClassPathResource("sql-scripts/populateAvailabilities.sql");
			Resource resource6 = new ClassPathResource("sql-scripts/populateServiceProviders.sql");
			try (Connection conn = dataSource.getConnection()) {
				ScriptUtils.executeSqlScript(conn, resource4);
			}
			System.out.println("Populated database");
		};
	}
 */


}

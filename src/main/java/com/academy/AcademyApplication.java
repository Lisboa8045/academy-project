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

//	@Bean
//	CommandLineRunner populateServices(DataSource dataSource) {
//		return args -> {
//			Resource resource = new ClassPathResource("sql-scripts/populateRoles.sql");
//			Resource resource2 = new ClassPathResource("sql-scripts/populateMembers.sql");
//			Resource resource3 = new ClassPathResource("sql-scripts/populateServices.sql");
//			Resource resource4 = new ClassPathResource("sql-scripts/populateAvailabilities.sql");
//			Resource resource6 = new ClassPathResource("sql-scripts/populateServiceProviders.sql");
//
//			try (Connection conn = dataSource.getConnection()) {
//				ScriptUtils.executeSqlScript(conn, resource);
//				ScriptUtils.executeSqlScript(conn, resource2);
//				ScriptUtils.executeSqlScript(conn, resource3);
//				ScriptUtils.executeSqlScript(conn, resource4);
//				ScriptUtils.executeSqlScript(conn, resource6);
//			}
//			System.out.println("Populated database");
//		};
//	}


}

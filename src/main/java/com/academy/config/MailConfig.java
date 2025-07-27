package com.academy.config;

import com.academy.services.GlobalConfigurationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

@Configuration
public class MailConfig {

    private JavaMailSenderImpl mailSender;

    @Bean
    public JavaMailSender javaMailSender() {
        if (mailSender == null) {
            mailSender = new JavaMailSenderImpl();
        }
        return mailSender;
    }

    @Bean
    @Order(2)
    public CommandLineRunner configureJavaMailSender(GlobalConfigurationService globalConfig) {
        return args -> {
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);
            mailSender.setUsername(globalConfig.getConfigValue("email"));
            mailSender.setPassword(globalConfig.getConfigValue("password"));

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        };
    }
}

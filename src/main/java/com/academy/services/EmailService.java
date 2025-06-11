package com.academy.services;

import com.academy.config.AppProperties;
import com.academy.exceptions.SendEmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService{

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final GlobalConfigurationService globalConfigurationService;

    public EmailService(JavaMailSender mailSender,
                        AppProperties appProperties,
                        GlobalConfigurationService globalConfigurationService) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
        this.globalConfigurationService = globalConfigurationService;
    }

    public void send(String to, String subject, String textContent, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, htmlContent);
            helper.setFrom(globalConfigurationService.getConfigValue("email"), appProperties.getName());

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new SendEmailException("Erro ao enviar e-mail");
        }
    }


}

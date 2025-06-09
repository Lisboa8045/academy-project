package com.academy.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Service
public class EmailService{

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String textContent, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, htmlContent);
            helper.setFrom("adriano.l.a.queiroz@gmail.com", "Minha Aplicação"); //TODO trocar estes 2 inputs

            mailSender.send(message);
        } catch (Exception e) { //TODO meter um erro melhor
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }


}

package com.academy.services;

import org.springframework.stereotype.Service;
@Service
public class EmailService{
/*
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String subject, String textContent, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, htmlContent);
            helper.setFrom("seuprojeto@gmail.com", "Minha Aplicação");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }

 */
}

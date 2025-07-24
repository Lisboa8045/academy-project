package com.academy.services;

import com.academy.config.AppProperties;
import com.academy.exceptions.EmailTemplateLoadingException;
import com.academy.exceptions.SendEmailException;
import com.academy.models.appointment.Appointment;
import com.academy.models.member.Member;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static com.academy.utils.Utils.formatHours;

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

    @Async
    protected void sendAppointmentConfirmationEmail(Appointment appointment) {
        String resetUrl = appProperties.getFrontendUrl() + "/confirm-appointment/" + appointment.getId();
        com.academy.models.service.Service service = appointment.getServiceProvider().getService();
        String html = loadEmailTemplate("templates/confirm-appointment.html")
                .replace("[SERVICE_NAME]", appointment.getMember().getUsername())
                .replace("[User Name]", appointment.getMember().getUsername())
                .replace("[ENTITY_NUMBER]", service.getEntity())
                .replace("[App Name]", appProperties.getName())
                .replace("[REFERENCE_NUMBER]", "123 456 789")
                .replace("[PAYMENT_CONFIRMATION_LINK]", resetUrl)
                .replace("[HOURS]", formatHours(globalConfigurationService.getConfigValue("confirm_appointment_expiry_minutes")))
                .replace("[AMOUNT]", Double.toString(service.getPrice()));

        send(
                appointment.getMember().getEmail(),
                "Confirm Appointment for Service: " + service.getName(),
                "Click on the link to proceed: " + resetUrl,
                html
        );
    }

    @Async
    protected void sendPasswordResetEmail(Member member, String rawToken) {
        String resetUrl = appProperties.getFrontendUrl() + "/reset-password/" + rawToken;

        String html = loadEmailTemplate("templates/password-reset-email.html")
                .replace("[User Name]", member.getUsername())
                .replace("[PASSWORD_RESET_LINK]", resetUrl)
                .replace("[App Name]", appProperties.getName())
                .replace("[HOURS]", formatHours(globalConfigurationService.getConfigValue("password_reset_token_expiry_minutes")));

        send(
                member.getEmail(),
                "Password Reset Request",
                "Click on the link to proceed: " + resetUrl,
                html
        );
    }

    @Async
    protected void sendConfirmationEmail(Member member, String rawToken) {
        String confirmationUrl = appProperties.getFrontendUrl() + "/confirm-email/" + rawToken;

        String html = loadEmailTemplate("templates/verification-email.html")
                .replace("[User Name]", member.getUsername())
                .replace("[CONFIRMATION_LINK]", confirmationUrl)
                .replace("[App Name]", appProperties.getName())
                .replace("[HOURS]", formatHours(globalConfigurationService.getConfigValue("confirmation_token_expiry_minutes")));

        send(
                member.getEmail(),
                "Confirm your account",
                "Click on the link to proceed: " + confirmationUrl,
                html
        );
    }

    @Async
    protected void sendCancelAppointmentEmail(Appointment appointment) {

        String html = loadEmailTemplate("templates/cancelled-appointment.html")
                .replace("[CLIENT_NAME]", appointment.getMember().getUsername())
                .replace("[APPOINTMENT_ID]", appointment.getId().toString())
                .replace("[PROVIDER_NAME]", appointment.getServiceProvider().getProvider().getUsername())
                .replace("[SERVICE_NAME]", appointment.getServiceProvider().getService().getName())
                .replace("[START_TIME]", appointment.getStartDateTime().toString())
                .replace("[App Name]", appProperties.getName());

                send(
                        appointment.getServiceProvider().getProvider().getEmail(),
                        "Canceled appointment",
                        "",
                        html
                );
                send(
                        appointment.getMember().getEmail(),
                        "Canceled appointment",
                        "",
                        html
                );
    }


    private String loadEmailTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            byte[] bytes = Files.readAllBytes(resource.getFile().toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EmailTemplateLoadingException("Error loading e-mail template: " + templatePath);
        }
    }

}

package com.academy.services;

import com.academy.models.appointment.Appointment;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.repositories.AppointmentRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Service
public class AppointmentSchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final AppointmentRepository appointmentRepository;

    public AppointmentSchedulerService(ThreadPoolTaskScheduler taskScheduler,
                                       AppointmentRepository appointmentRepository) {
        this.taskScheduler = taskScheduler;
        this.appointmentRepository = appointmentRepository;
    }

    public void scheduleAutoCancel(Appointment appointment, int delayMinutes) {
        LocalDateTime executionTime = appointment.getCreatedAt().plusMinutes(delayMinutes);
        long delayMillis = Duration.between(LocalDateTime.now(), executionTime).toMillis();

        taskScheduler.schedule(
                () -> autoCancelIfStillPending(appointment.getId()),
                new Date(System.currentTimeMillis() + delayMillis)
        );
    }

    private void autoCancelIfStillPending(Long appointmentId) {
        try {
            appointmentRepository.cancelIfStillPending(appointmentId, AppointmentStatus.CANCELLED);
            System.out.println("Appointment " + appointmentId + " checked for auto-cancel.");
        } catch (Exception ex) {
            System.err.println("Failed to auto-cancel appointment " + appointmentId + ": " + ex.getMessage());
        }
    }
}
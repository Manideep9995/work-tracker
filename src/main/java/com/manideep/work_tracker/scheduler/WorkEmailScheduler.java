package com.manideep.worktracker.scheduler;

import com.manideep.work_tracker.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorkEmailScheduler {

    private final EmailService emailService;

    public WorkEmailScheduler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendMorningWorkSummary() {

        System.out.println("Running morning work summary...");

        emailService.sendMorningSummary();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    public void sendMidnightWorkSummary() {

        System.out.println("Running midnight work summary...");

        emailService.sendPreviousDaySummary();
    }
}
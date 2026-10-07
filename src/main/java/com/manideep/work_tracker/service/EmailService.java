package com.manideep.work_tracker.service;

import com.manideep.work_tracker.entity.WorkItem;
import com.manideep.work_tracker.entity.WorkStatus;
import com.manideep.work_tracker.Repo.WorkItemRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final WorkItemRepository workItemRepository;

    @Value("${spring.mail.username}")
    private String mailFrom;
    @Value("${work.tracker.email.to}")
    private String mailTo;

    public EmailService(
            JavaMailSender mailSender,
            WorkItemRepository workItemRepository) {

        this.mailSender = mailSender;
        this.workItemRepository = workItemRepository;
    }

    public void sendWorkSummary() {

        List<WorkItem> works =
                workItemRepository.findAllByOrderByCreatedAtDesc();

        long total = works.size();

        long completed = works.stream()
                .filter(work ->
                        work.getStatus() == WorkStatus.COMPLETED)
                .count();

        long pending = works.stream()
                .filter(work ->
                        work.getStatus() == WorkStatus.PENDING)
                .count();

        String html = buildEmailHtml(
                works,
                total,
                completed,
                pending
        );

        sendEmail(
                mailFrom,
                "Daily Work Summary - " + LocalDate.now(),
                html
        );
    }

    private void sendEmail(
            String to,
            String subject,
            String html) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Failed to send email",
                    e
            );
        }
    }

    private String buildEmailHtml(
            List<WorkItem> works,
            long total,
            long completed,
            long pending) {

        StringBuilder html = new StringBuilder();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd-MMM-yyyy hh:mm a"
                );

        html.append("""
                <html>
                <body style="font-family: Arial, sans-serif;">

                <h2>Daily Work Summary</h2>

                <p>Here is your current work summary.</p>

                <table border="1"
                       cellpadding="8"
                       cellspacing="0">

                    <tr>
                        <th>Total</th>
                        <th>Completed</th>
                        <th>Pending</th>
                    </tr>

                """);

        html.append("<tr>");

        html.append("<td>")
                .append(total)
                .append("</td>");

        html.append("<td>")
                .append(completed)
                .append("</td>");

        html.append("<td>")
                .append(pending)
                .append("</td>");

        html.append("</tr>");

        html.append("</table>");

        html.append("<h3>Work Items</h3>");

        html.append("""
                <table border="1"
                       cellpadding="8"
                       cellspacing="0">

                    <tr>
                        <th>Work</th>
                        <th>Status</th>
                        <th>Created</th>
                        <th>Completed</th>
                    </tr>
                """);

        for (WorkItem work : works) {

            html.append("<tr>");

            html.append("<td>")
                    .append(escapeHtml(work.getTitle()))
                    .append("</td>");

            html.append("<td>")
                    .append(work.getStatus())
                    .append("</td>");

            html.append("<td>")
                    .append(work.getCreatedAt()
                            .format(formatter))
                    .append("</td>");

            html.append("<td>");

            if (work.getCompletedAt() != null) {

                html.append(
                        work.getCompletedAt()
                                .format(formatter)
                );

            } else {

                html.append("-");

            }

            html.append("</td>");

            html.append("</tr>");
        }

        html.append("""
                </table>

                <br>

                <p>
                    This email was generated automatically
                    by Work Tracker.
                </p>

                </body>
                </html>
                """);

        return html.toString();
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    public void sendMorningSummary() {

        LocalDate today = LocalDate.now(zoneId);

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<WorkItem> pendingWorks =
                workItemRepository.findByStatusOrderByCreatedAtDesc(
                        WorkStatus.PENDING
                );

        List<WorkItem> completedToday =
                workItemRepository.findByCompletedAtBetweenOrderByCompletedAtDesc(
                        startOfDay,
                        endOfDay
                );

        String html = buildDailySummaryHtml(
                "Good Morning ☀️",
                pendingWorks,
                completedToday,
                false
        );

        sendEmail(
                mailTo,
                "Work Tracker - Morning Summary",
                html
        );
    }

    public void sendPreviousDaySummary() {

        LocalDate today = LocalDate.now(zoneId);

        // At 12:00 AM, we want yesterday's information
        LocalDate previousDay = today.minusDays(1);

        LocalDateTime startOfDay = previousDay.atStartOfDay();
        LocalDateTime endOfDay = today.atStartOfDay();

        List<WorkItem> pendingWorks =
                workItemRepository.findByStatusOrderByCreatedAtDesc(
                        WorkStatus.PENDING
                );

        List<WorkItem> completedPreviousDay =
                workItemRepository.findByCompletedAtBetweenOrderByCompletedAtDesc(
                        startOfDay,
                        endOfDay
                );

        String html = buildDailySummaryHtml(
                "End of Day Summary 🌙",
                pendingWorks,
                completedPreviousDay,
                true
        );

        sendEmail(
                mailTo,
                "Work Tracker - End of Day Summary",
                html
        );
    }
    private String buildDailySummaryHtml(
            String heading,
            List<WorkItem> pendingWorks,
            List<WorkItem> completedWorks,
            boolean endOfDay
    ) {

        StringBuilder html = new StringBuilder();

        html.append("""
            <html>
            <body style="font-family: Arial, sans-serif;">

            <h2>%s</h2>

            <h3>Pending Work: %d</h3>
            """.formatted(heading, pendingWorks.size()));

        if (pendingWorks.isEmpty()) {

            html.append("<p>No pending work 🎉</p>");

        } else {

            html.append("""
                <table border="1"
                       cellpadding="8"
                       cellspacing="0"
                       style="border-collapse: collapse;">

                    <tr>
                        <th>Work</th>
                        <th>Description</th>
                        <th>Created</th>
                    </tr>
                """);

            for (WorkItem work : pendingWorks) {

                html.append("""
                    <tr>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                        escapeHtml(work.getTitle()),
                        escapeHtml(work.getDescription()),
                        work.getCreatedAt()
                ));
            }

            html.append("</table>");
        }

        html.append("""

            <br>

            <h3>Completed Work: %d</h3>
            """.formatted(completedWorks.size()));

        if (completedWorks.isEmpty()) {

            html.append("<p>No work was completed.</p>");

        } else {

            html.append("""
                <table border="1"
                       cellpadding="8"
                       cellspacing="0"
                       style="border-collapse: collapse;">

                    <tr>
                        <th>Work</th>
                        <th>Description</th>
                        <th>Created</th>
                        <th>Completed</th>
                    </tr>
                """);

            for (WorkItem work : completedWorks) {

                html.append("""
                    <tr>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                        escapeHtml(work.getTitle()),
                        escapeHtml(work.getDescription()),
                        work.getCreatedAt(),
                        work.getCompletedAt()
                ));
            }

            html.append("</table>");
        }

        if (endOfDay) {
            html.append("""

                <br>
                <p><b>This is your end-of-day summary.</b></p>
                """);
        }

        html.append("""

            </body>
            </html>
            """);

        return html.toString();
    }
}

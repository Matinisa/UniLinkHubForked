package za.co.unilinkhub.report.application;

import za.co.unilinkhub.report.domain.Report;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Report + resolved reporter/target info, for the admin review queue. ReportDTO stays a plain
 * projection of the aggregate (used for the student's own "my reports" list); this is the
 * cross-module composed view admins actually need to act on a report without a raw UUID.
 */
public record AdminReportView(
        UUID id,
        String reason,
        String details,
        String status,
        String adminNote,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt,
        ReporterSummary reporter,
        TargetSummary target
) {
    public static AdminReportView of(Report report, ReporterSummary reporter, TargetSummary target) {
        return new AdminReportView(
                report.getId(), report.getReason().name(), report.getDetails(), report.getStatus().name(),
                report.getAdminNote(), report.getCreatedAt(), report.getResolvedAt(), reporter, target
        );
    }
}

package za.co.unilinkhub.report.repository;

import za.co.unilinkhub.report.domain.Report;
import za.co.unilinkhub.report.domain.ReportStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(UUID id);

    List<Report> findAll();

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByReporterId(UUID reporterId);

    long countByStatus(ReportStatus status);
}

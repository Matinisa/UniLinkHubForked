package za.co.unilinkhub.report.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.unilinkhub.common.exception.ResourceNotFoundException;
import za.co.unilinkhub.report.domain.Report;
import za.co.unilinkhub.report.domain.ReportReason;
import za.co.unilinkhub.report.domain.ReportStatus;
import za.co.unilinkhub.report.domain.ReportTargetType;
import za.co.unilinkhub.report.repository.ReportRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportDTO file(UUID reporterId, ReportTargetType targetType, UUID targetId, ReportReason reason, String details) {
        Report report = Report.file(reporterId, targetType, targetId, reason, details);
        return ReportDTO.from(reportRepository.save(report));
    }

    public List<ReportDTO> myReports(UUID reporterId) {
        return reportRepository.findByReporterId(reporterId).stream().map(ReportDTO::from).toList();
    }

    public List<ReportDTO> queue(ReportStatus status) {
        return reportRepository.findByStatus(status).stream().map(ReportDTO::from).toList();
    }

    public ReportDTO beginReview(UUID reportId, UUID adminId) {
        Report report = findReport(reportId);
        report.beginReview(adminId);
        return ReportDTO.from(reportRepository.save(report));
    }

    public ReportDTO resolve(UUID reportId, UUID adminId, String note) {
        Report report = findReport(reportId);
        report.resolve(adminId, note);
        return ReportDTO.from(reportRepository.save(report));
    }

    public ReportDTO dismiss(UUID reportId, UUID adminId, String note) {
        Report report = findReport(reportId);
        report.dismiss(adminId, note);
        return ReportDTO.from(reportRepository.save(report));
    }

    private Report findReport(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }
}

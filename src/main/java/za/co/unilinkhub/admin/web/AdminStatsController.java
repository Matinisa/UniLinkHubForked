package za.co.unilinkhub.admin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.unilinkhub.admin.application.AdminStatsDTO;
import za.co.unilinkhub.admin.application.AdminStatsService;

@RestController
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatsDTO stats() {
        return adminStatsService.compute();
    }
}

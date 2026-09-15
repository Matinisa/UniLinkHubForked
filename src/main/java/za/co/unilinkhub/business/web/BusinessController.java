package za.co.unilinkhub.business.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.unilinkhub.business.application.BusinessDTO;
import za.co.unilinkhub.business.application.BusinessService;
import za.co.unilinkhub.security.CurrentUser;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    public record CreateBusinessRequest(
            @NotBlank String businessName,
            @NotBlank String description,
            @NotBlank String category
    ) {
    }

    public record UpdateBusinessRequest(String businessName, String description, String category) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessDTO create(@CurrentUser UUID userId, @Valid @RequestBody CreateBusinessRequest request) {
        return businessService.create(userId, request.businessName(), request.description(), request.category());
    }

    @PatchMapping("/{id}")
    public BusinessDTO update(@CurrentUser UUID userId, @PathVariable UUID id, @RequestBody UpdateBusinessRequest request) {
        return businessService.update(id, userId, request.businessName(), request.description(), request.category());
    }

    @PostMapping("/{id}/request-verification")
    public BusinessDTO requestVerification(@CurrentUser UUID userId, @PathVariable UUID id) {
        return businessService.requestVerification(id, userId);
    }

    @GetMapping("/{id}")
    public BusinessDTO getById(@PathVariable UUID id) {
        return businessService.getById(id);
    }

    @GetMapping("/mine")
    public List<BusinessDTO> mine(@CurrentUser UUID userId) {
        return businessService.getByOwner(userId);
    }
}

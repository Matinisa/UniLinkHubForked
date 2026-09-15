package za.co.unilinkhub.business.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.unilinkhub.business.domain.Business;
import za.co.unilinkhub.business.repository.BusinessRepository;
import za.co.unilinkhub.common.exception.ResourceNotFoundException;
import za.co.unilinkhub.common.exception.UnauthorizedException;
import za.co.unilinkhub.user.application.UserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserService userService;

    public BusinessDTO create(UUID ownerId, String businessName, String description, String category) {
        // Becoming a seller and registering a first business happen together for the MVP flow.
        userService.becomeSeller(ownerId);
        Business business = Business.create(ownerId, businessName, description, category);
        return BusinessDTO.from(businessRepository.save(business));
    }

    public BusinessDTO update(UUID businessId, UUID requesterId, String businessName, String description, String category) {
        Business business = findOwned(businessId, requesterId);
        business.updateDetails(businessName, description, category);
        return BusinessDTO.from(businessRepository.save(business));
    }

    public BusinessDTO requestVerification(UUID businessId, UUID requesterId) {
        Business business = findOwned(businessId, requesterId);
        business.requestVerification();
        return BusinessDTO.from(businessRepository.save(business));
    }

    public BusinessDTO getById(UUID id) {
        return BusinessDTO.from(businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found")));
    }

    public List<BusinessDTO> getByOwner(UUID ownerId) {
        return businessRepository.findByOwnerId(ownerId).stream().map(BusinessDTO::from).toList();
    }

    private Business findOwned(UUID businessId, UUID requesterId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        if (!business.getOwnerId().equals(requesterId)) {
            throw new UnauthorizedException("You do not own this business");
        }
        return business;
    }
}

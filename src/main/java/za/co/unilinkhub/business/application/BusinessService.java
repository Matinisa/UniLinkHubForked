package za.co.unilinkhub.business.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.unilinkhub.business.domain.Business;
import za.co.unilinkhub.business.domain.VerificationStatus;
import za.co.unilinkhub.business.repository.BusinessRepository;
import za.co.unilinkhub.common.exception.ResourceNotFoundException;
import za.co.unilinkhub.common.exception.UnauthorizedException;
import za.co.unilinkhub.listing.domain.Listing;
import za.co.unilinkhub.listing.domain.ListingStatus;
import za.co.unilinkhub.listing.repository.ListingRepository;
import za.co.unilinkhub.user.application.UserDTO;
import za.co.unilinkhub.user.application.UserService;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final ListingRepository listingRepository;
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
        return BusinessDTO.from(findById(id));
    }

    public ProviderProfileDTO getProviderProfile(UUID businessId) {
        Business business = findById(businessId);
        UserDTO owner = userService.getById(business.getOwnerId());
        List<Listing> listings = listingRepository.findByBusinessId(businessId);

        long activeCount = listings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count();
        long totalViews = listings.stream().mapToLong(Listing::getViewCount).sum();

        return new ProviderProfileDTO(
                business.getId(), business.getBusinessName(), business.getDescription(), business.getCategory(),
                business.getVerificationStatus().name(), business.getOwnerId(),
                owner.firstName() + " " + owner.lastName(), activeCount, totalViews, business.getCreatedAt()
        );
    }

    public List<BusinessDTO> getByOwner(UUID ownerId) {
        return businessRepository.findByOwnerId(ownerId).stream().map(BusinessDTO::from).toList();
    }

    public List<AdminBusinessView> listByStatus(VerificationStatus status) {
        return businessRepository.findByVerificationStatus(status).stream()
                .sorted(Comparator.comparing(Business::getCreatedAt))
                .map(this::toAdminView)
                .toList();
    }

    public List<AdminBusinessView> recentlyDecided(int limit) {
        return businessRepository.findByVerificationStatusNot(VerificationStatus.PENDING).stream()
                .sorted(Comparator.comparing(Business::getUpdatedAt).reversed())
                .limit(limit)
                .map(this::toAdminView)
                .toList();
    }

    public BusinessDTO verify(UUID businessId) {
        Business business = findById(businessId);
        business.verify();
        return BusinessDTO.from(businessRepository.save(business));
    }

    public BusinessDTO reject(UUID businessId) {
        Business business = findById(businessId);
        business.reject();
        return BusinessDTO.from(businessRepository.save(business));
    }

    private Business findById(UUID id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
    }

    private Business findOwned(UUID businessId, UUID requesterId) {
        Business business = findById(businessId);
        if (!business.getOwnerId().equals(requesterId)) {
            throw new UnauthorizedException("You do not own this business");
        }
        return business;
    }

    private AdminBusinessView toAdminView(Business business) {
        return AdminBusinessView.from(business, userService.getById(business.getOwnerId()));
    }
}

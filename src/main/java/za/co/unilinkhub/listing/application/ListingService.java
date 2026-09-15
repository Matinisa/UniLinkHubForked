package za.co.unilinkhub.listing.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.unilinkhub.business.domain.Business;
import za.co.unilinkhub.business.repository.BusinessRepository;
import za.co.unilinkhub.common.exception.ResourceNotFoundException;
import za.co.unilinkhub.common.exception.UnauthorizedException;
import za.co.unilinkhub.listing.domain.Listing;
import za.co.unilinkhub.listing.domain.Product;
import za.co.unilinkhub.listing.repository.ListingRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final BusinessRepository businessRepository;

    public ListingDTO createProduct(UUID requesterId, UUID businessId, String name, String description,
                                     String category, BigDecimal price, Integer stockQuantity, String imageUrl) {
        assertOwnership(businessId, requesterId);
        Product product = Product.create(businessId, name, description, category, price, stockQuantity, imageUrl);
        return ListingDTO.from(listingRepository.save(product));
    }

    public ListingDTO createService(UUID requesterId, UUID businessId, String name, String description,
                                     String category, BigDecimal price, Integer durationMinutes,
                                     String availabilitySchedule) {
        assertOwnership(businessId, requesterId);
        za.co.unilinkhub.listing.domain.Service service = za.co.unilinkhub.listing.domain.Service.create(
                businessId, name, description, category, price, durationMinutes, availabilitySchedule);
        return ListingDTO.from(listingRepository.save(service));
    }

    public ListingDTO update(UUID requesterId, UUID listingId, String name, String description,
                              String category, BigDecimal price) {
        Listing listing = findListing(listingId);
        assertOwnership(listing.getBusinessId(), requesterId);
        listing.updateBasicDetails(name, description, category, price);
        return ListingDTO.from(listingRepository.save(listing));
    }

    public void deactivate(UUID requesterId, UUID listingId) {
        Listing listing = findListing(listingId);
        assertOwnership(listing.getBusinessId(), requesterId);
        listing.deactivate();
        listingRepository.save(listing);
    }

    @Transactional
    public ListingDTO getById(UUID id) {
        Listing listing = findListing(id);
        listing.recordView();
        return ListingDTO.from(listingRepository.save(listing));
    }

    public List<ListingDTO> search(String category, String keyword) {
        return listingRepository.search(category, keyword).stream().map(ListingDTO::from).toList();
    }

    public List<ListingDTO> byBusiness(UUID businessId) {
        return listingRepository.findByBusinessId(businessId).stream().map(ListingDTO::from).toList();
    }

    private Listing findListing(UUID id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }

    private void assertOwnership(UUID businessId, UUID requesterId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        if (!business.getOwnerId().equals(requesterId)) {
            throw new UnauthorizedException("You do not own this business");
        }
    }
}

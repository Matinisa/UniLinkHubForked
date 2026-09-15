package za.co.unilinkhub.listing.application;

import za.co.unilinkhub.listing.domain.Listing;
import za.co.unilinkhub.listing.domain.Product;
import za.co.unilinkhub.listing.domain.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ListingDTO(
        UUID id,
        UUID businessId,
        String type,
        String name,
        String description,
        String category,
        BigDecimal price,
        String status,
        long viewCount,
        Integer stockQuantity,
        String imageUrl,
        Integer durationMinutes,
        String availabilitySchedule,
        LocalDateTime createdAt
) {
    public static ListingDTO from(Listing listing) {
        Integer stockQuantity = null;
        String imageUrl = null;
        Integer durationMinutes = null;
        String availabilitySchedule = null;
        String type;

        if (listing instanceof Product product) {
            type = "PRODUCT";
            stockQuantity = product.getStockQuantity();
            imageUrl = product.getImageUrl();
        } else if (listing instanceof Service service) {
            type = "SERVICE";
            durationMinutes = service.getDurationMinutes();
            availabilitySchedule = service.getAvailabilitySchedule();
        } else {
            type = "UNKNOWN";
        }

        return new ListingDTO(
                listing.getId(), listing.getBusinessId(), type, listing.getName(),
                listing.getDescription(), listing.getCategory(), listing.getPrice(),
                listing.getStatus().name(), listing.getViewCount(),
                stockQuantity, imageUrl, durationMinutes, availabilitySchedule,
                listing.getCreatedAt()
        );
    }
}

package za.co.unilinkhub.business.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.unilinkhub.business.domain.Business;
import za.co.unilinkhub.business.repository.BusinessRepository;

import java.util.List;
import java.util.UUID;

public interface JpaBusinessRepository extends JpaRepository<Business, UUID>, BusinessRepository {

    @Override
    List<Business> findByOwnerId(UUID ownerId);
}

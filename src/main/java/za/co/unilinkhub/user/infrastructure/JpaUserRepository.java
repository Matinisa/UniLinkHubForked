package za.co.unilinkhub.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.unilinkhub.user.domain.AccountStatus;
import za.co.unilinkhub.user.domain.User;
import za.co.unilinkhub.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: Spring Data generates the implementation of both JpaRepository and the
 * hand-written UserRepository port from these method signatures.
 */
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {

    @Override
    Optional<User> findByEmail(String email);

    @Override
    Optional<User> findByStudentNumber(String studentNumber);

    @Override
    Optional<User> findByVerificationToken(String verificationToken);

    @Override
    List<User> findByAccountStatus(AccountStatus accountStatus);

    @Override
    boolean existsByEmail(String email);

    @Override
    boolean existsByStudentNumber(String studentNumber);
}

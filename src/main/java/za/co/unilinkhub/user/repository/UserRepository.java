package za.co.unilinkhub.user.repository;

import za.co.unilinkhub.user.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Port: what the application layer needs from persistence, independent of Spring Data / JPA.
 * {@link za.co.unilinkhub.user.infrastructure.JpaUserRepository} is the adapter that implements it.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByStudentNumber(String studentNumber);

    Optional<User> findByVerificationToken(String verificationToken);

    boolean existsByEmail(String email);

    boolean existsByStudentNumber(String studentNumber);
}

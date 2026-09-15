package za.co.unilinkhub.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A verified member of the university community. Every user starts as a buyer; calling
 * {@link #becomeSeller()} unlocks listing creation on the same account (Section 13.2 of the
 * project docs) rather than requiring a second, separate seller account.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_student_number", columnNames = "student_number")
})
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_number", nullable = false, length = 32)
    private String studentNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    @Column(name = "is_seller", nullable = false)
    private boolean seller = false;

    @Column(name = "verification_token", length = 64)
    private String verificationToken;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private User(String studentNumber, String firstName, String lastName, String email,
                  String passwordHash, String verificationToken) {
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.verificationToken = verificationToken;
    }

    public static User register(String studentNumber, String firstName, String lastName,
                                 String email, String passwordHash, String verificationToken) {
        return new User(studentNumber, firstName, lastName, email, passwordHash, verificationToken);
    }

    public void verifyEmail(String token) {
        if (this.accountStatus != AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Account is not pending verification");
        }
        if (this.verificationToken == null || !this.verificationToken.equals(token)) {
            throw new IllegalArgumentException("Invalid verification token");
        }
        this.accountStatus = AccountStatus.ACTIVE;
        this.verificationToken = null;
    }

    public void becomeSeller() {
        this.seller = true;
    }

    public void suspend() {
        this.accountStatus = AccountStatus.SUSPENDED;
    }

    public void deactivate() {
        this.accountStatus = AccountStatus.DEACTIVATED;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

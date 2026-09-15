package za.co.unilinkhub.user.application;

import za.co.unilinkhub.user.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String studentNumber,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String role,
        String accountStatus,
        boolean seller,
        LocalDateTime createdAt
) {
    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getStudentNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getAccountStatus().name(),
                user.isSeller(),
                user.getCreatedAt()
        );
    }
}

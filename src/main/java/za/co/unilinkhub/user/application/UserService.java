package za.co.unilinkhub.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.unilinkhub.common.exception.BadRequestException;
import za.co.unilinkhub.common.exception.ResourceNotFoundException;
import za.co.unilinkhub.user.domain.User;
import za.co.unilinkhub.user.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getById(UUID id) {
        return UserDTO.from(findUser(id));
    }

    public UserDTO verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));
        user.verifyEmail(token);
        return UserDTO.from(userRepository.save(user));
    }

    public UserDTO becomeSeller(UUID userId) {
        User user = findUser(userId);
        user.becomeSeller();
        return UserDTO.from(userRepository.save(user));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

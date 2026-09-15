package za.co.unilinkhub.user.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.unilinkhub.security.CurrentUser;
import za.co.unilinkhub.user.application.UpdateProfileUseCase;
import za.co.unilinkhub.user.application.UserDTO;
import za.co.unilinkhub.user.application.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UpdateProfileUseCase updateProfileUseCase;

    @GetMapping("/me")
    public UserResponse me(@CurrentUser UUID userId) {
        return UserResponse.from(userService.getById(userId));
    }

    @PatchMapping("/me")
    public UserResponse updateProfile(@CurrentUser UUID userId, @Valid @RequestBody UserRequest.UpdateProfile request) {
        UserDTO updated = updateProfileUseCase.execute(userId, request.firstName(), request.lastName(), request.phoneNumber());
        return UserResponse.from(updated);
    }

    @PostMapping("/me/become-seller")
    public UserResponse becomeSeller(@CurrentUser UUID userId) {
        return UserResponse.from(userService.becomeSeller(userId));
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        return UserResponse.from(userService.getById(id));
    }
}

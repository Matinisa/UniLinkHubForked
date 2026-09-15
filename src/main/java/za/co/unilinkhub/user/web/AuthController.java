package za.co.unilinkhub.user.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.unilinkhub.security.JwtService;
import za.co.unilinkhub.security.UserPrincipal;
import za.co.unilinkhub.user.application.RegisterUserUseCase;
import za.co.unilinkhub.user.application.UserDTO;
import za.co.unilinkhub.user.application.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRequest.Register request) {
        UserDTO user = registerUserUseCase.execute(
                request.studentNumber(), request.firstName(), request.lastName(),
                request.email(), request.password()
        );
        return UserResponse.from(user);
    }

    @GetMapping("/verify")
    public UserResponse verify(@RequestParam String token) {
        return UserResponse.from(userService.verifyEmail(token));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody UserRequest.Login request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        UserDTO userDTO = userService.getById(principal.getId());
        return new AuthResponse(token, UserResponse.from(userDTO));
    }
}

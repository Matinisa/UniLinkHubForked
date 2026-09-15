package za.co.unilinkhub.user.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequest {

    public record Register(
            @NotBlank String studentNumber,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password
    ) {
    }

    public record Login(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record UpdateProfile(
            String firstName,
            String lastName,
            String phoneNumber
    ) {
    }

    public record ChangePassword(
            @NotBlank String currentPassword,
            @NotBlank String newPassword
    ) {
    }
}

package com.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDto {
    @NotBlank(message = "Login is required")
    private String login;
    @NotBlank(message = "Password is required")
    private String password;
    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Surname is mandatory")
    private String surname;
    @Email(message = "Email should be valid")
    private String email;
    private LocalDate birthDate;
}

package com.org.gigscore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email;

    @NotBlank(message = "Password is required")
    String password;
}
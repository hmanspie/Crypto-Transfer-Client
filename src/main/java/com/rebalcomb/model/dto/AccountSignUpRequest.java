package com.rebalcomb.model.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountSignUpRequest {

    @NotBlank
    @Size(min = 3, max = 25, message = "Invalid login")
    String login;

    @NotBlank
    @Size(min = 6, max = 15, message = "Invalid fullName")
    String fullName;

    @NotBlank
    @Email
    String email;

    @NotBlank
    @Size(max = 50, min = 8, message = "Invalid password")
    String passwd;

    @NotBlank
    String confirmPassword;
}

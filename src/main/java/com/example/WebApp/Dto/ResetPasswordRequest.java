package com.example.WebApp.Dto;

import lombok.*;

@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ResetPasswordRequest {

    private String email;

    private String token;

    private String password;

    private String repeatPassword;
}

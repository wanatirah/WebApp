package com.example.WebApp.Dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class UserDataRequest implements Serializable {

    @NotEmpty(message = "{registration.validation.firstName}")
    private String firstName;

    @NotEmpty(message = "{registration.validation.lastName}")
    private String lastName;

    @NotEmpty(message = "{registration.validation.email}")
    private String email;

    @NotEmpty(message = "{registration.validation.password}")
    private String password;
}

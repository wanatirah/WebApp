package com.example.WebApp.AppUser;

import com.example.WebApp.Security.SecureToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;
    private Boolean accountVerified;

    @OneToMany(mappedBy = "user")
    private Set<SecureToken> tokens;

    public boolean isAccountVerified() {
        return accountVerified;
    }
}

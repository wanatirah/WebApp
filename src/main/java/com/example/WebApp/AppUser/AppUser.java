package com.example.WebApp.AppUser;

import com.example.WebApp.Security.SecureToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class AppUser implements UserDetails {

    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    private Boolean locked = false;

    private Boolean enabled = false;

    private Boolean accountVerified;

    private String token;

    @OneToMany(mappedBy = "user")
    private Set<SecureToken> tokens;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<UserRole> userRoles = new HashSet<>();

    public AppUser(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public void addUserRoles(UserRole role){
        userRoles.add(role);
        role.getUsers().add(this);
    }

    public void removeUserRoles(UserRole role){
        userRoles.remove(role);
        role.getUsers().remove(this);
    }

    public void addToken(SecureToken token){
        tokens.add(token);
    }

    public boolean isAccountVerified() {
        return accountVerified;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        userRoles = getUserRoles();
        Collection<GrantedAuthority> authorities = new ArrayList<>(userRoles.size());
        for(UserRole userRole : userRoles){
            authorities.add(new SimpleGrantedAuthority(userRole.getCode().toUpperCase()));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

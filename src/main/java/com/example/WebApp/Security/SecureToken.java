package com.example.WebApp.Security;

import com.example.WebApp.AppUser.AppUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class SecureToken {

    @SequenceGenerator(
            name = "token_sequence",
            sequenceName = "token_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "token_sequence"
    )
    private Long id;

    @Column(unique = true)
    private String token;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp timestamp;

    @Column(updatable = false)
    @Basic(optional = false)
    private LocalDateTime expireAt;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName ="id")
    private AppUser user;

    @Transient
    private Boolean isExpired;

    public SecureToken(String token, Timestamp timestamp, LocalDateTime expireAt, AppUser user) {
        this.token = token;
        this.timestamp = timestamp;
        this.expireAt = expireAt;
        this.user = user;
    }

    public boolean isExpired() {
        return getExpireAt().isBefore(LocalDateTime.now());
    }
}

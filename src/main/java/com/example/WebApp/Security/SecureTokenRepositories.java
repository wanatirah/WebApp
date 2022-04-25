package com.example.WebApp.Security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecureTokenRepositories extends JpaRepository<SecureToken, Long> {

    SecureToken findByToken(String token);

    Long removeByToken(String token);
}

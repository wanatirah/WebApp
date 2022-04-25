package com.example.WebApp.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepositories extends JpaRepository<AppUser, Long> {

    AppUser findByEmail(String email);
}

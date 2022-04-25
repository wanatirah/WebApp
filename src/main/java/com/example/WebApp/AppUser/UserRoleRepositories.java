package com.example.WebApp.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepositories extends JpaRepository<UserRole, Long> {

    UserRole findByCode(String code);
}

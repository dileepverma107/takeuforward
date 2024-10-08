package com.takuforward.takuforward.respository;

import com.takuforward.takuforward.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByEmail(String email);

}
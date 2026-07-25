package com.org.gigscore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.gigscore.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    java.util.Optional<User> findByEmail(String email);
    
}

package com.meloncity.citiz.repository;

import com.meloncity.citiz.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    boolean existsByEmail(String email);
    Optional<Profile> findByEmail(String email);
}

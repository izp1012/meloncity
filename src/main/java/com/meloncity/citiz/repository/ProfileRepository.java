package com.meloncity.citiz.repository;

import com.meloncity.citiz.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    boolean existsByEmail(String email);

    Page<Profile> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Profile> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Profile> findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(String name, String email, Pageable pageable);
    Optional<Profile> findByEmail(String email);
    boolean existsById(Long id);
}

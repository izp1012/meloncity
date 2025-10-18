package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ProfileService profileService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Profile profile = profileService.findByEmail(email);
        return org.springframework.security.core.userdetails.User
                .withUsername(profile.getEmail())
                .password(profile.getPassword())
                .authorities(profile.getRole())
                .build();
    }
}

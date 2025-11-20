package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ProfileService profileService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Profile profile = profileService.findByEmail(email);
        return CustomUserDetails.builder()
                .id(profile.getId())
                .username(profile.getEmail())
                .password(profile.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(profile.getRole())))
                .build();
    }
}

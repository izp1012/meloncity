package com.meloncity.citiz.config;

import com.meloncity.citiz.repository.ProfileRepository;
import com.meloncity.citiz.security.jwt.JwtAuthFilter;
import com.meloncity.citiz.security.jwt.JwtRefreshAuthFilter;
import com.meloncity.citiz.security.jwt.JwtTokenProvider;
import com.meloncity.citiz.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileRepository profileRepository;

    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // ✅ CSRF 체크 비활성화
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //.requestMatchers("/users/**").permitAll()
//                        .anyRequest().authenticated()
                        .anyRequest().permitAll()                 // 나머지도 전부 허용 (개발 단계용)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAfter(jwtRefreshAuthFilter(), LogoutFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public JwtRefreshAuthFilter jwtRefreshAuthFilter(){
        return new JwtRefreshAuthFilter(jwtTokenProvider, profileRepository);
    }

}

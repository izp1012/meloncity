package com.meloncity.citiz.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.repository.ProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtRefreshAuthFilter extends OncePerRequestFilter {

    private static final String MATCH_REQUEST_URL = "/api/users/login-extension";
    private static final String HTTP_METHOD = "POST";

    @Value("${spring.profiles.active}") private String PROFILES_ACTIVE;
    @Value("${jwt.access.header}") private String ACCESS_TOKEN_HEADER;
    @Value("${jwt.refresh.header}") private String REFRESH_TOKEN_HEADER;
    @Value("${jwt.refresh.expiration}") private int REFRESH_TOKEN_EXPRIRE_DATE;

    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileRepository profileRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // refresh 토큰 재발급 요청인 경우만 검증
        if(!MATCH_REQUEST_URL.equals(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        // 잘못된 메소드 요청인 경우
        if(!HTTP_METHOD.equals(request.getMethod())){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("잘못된 요청입니다.");
            return ;
        }

        try{
            String refreshToken = jwtTokenProvider.extractRefreshToken(request).orElseThrow(() -> new NullPointerException());
            TokenValidationResult valid = jwtTokenProvider.isValidJwt(refreshToken);

            if(valid != TokenValidationResult.SUCCESS){
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("유효하지 않은 토큰입니다.");
                log.info("Access Denied : RefreshToken이 유효하지 않습니다. serverName : {}", request.getServerName());

                return ;
            }else{
                checkRefreshTokenAndReIssueAccessToken(request, response, refreshToken);
            }

        }catch (NullPointerException e){
            e.printStackTrace();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("REFRESH TOKEN이 없습니다.");
            return;
        }

    }

    private void checkRefreshTokenAndReIssueAccessToken(HttpServletRequest request, HttpServletResponse response, String refreshToken) throws IOException {
        //레디스에 저장된 토큰인지 아닌지 검증
        // 해당 토큰은 유출되었다고 판단 -> 추후 해당 세션을 막는 로직 필요

        String profileId = jwtTokenProvider.getSubject(refreshToken);
        Optional<Profile> profile = profileRepository.findByEmail(profileId);

        if(profile.isEmpty()){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("유효하지 않은 사용자입니다.");
            log.info("Access Denied : 사용자ID가 유효하지 않습니다. ID : {}", profileId);

            return;
        }else if(!jwtTokenProvider.chkRefreshToken(profileId, refreshToken)){
            // 레디스에 있는 토큰과 동일한지 확인
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("발급된 토큰과 다릅니다.");
            log.info("Access Denied : 발급된 토큰과 다릅니다.");

            return;
        }

        List<String> roles = Collections.singletonList(profile.get().getRole());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        String newAccessToken = jwtTokenProvider.createToken(profileId, roles);
        String reIssueRefreshToken = jwtTokenProvider.reIssueRefreshToken(profileId, roles);

        Cookie cookie = jwtTokenProvider.createCookie(reIssueRefreshToken);
        response.addCookie(cookie);

        response.getWriter().write(
                new ObjectMapper().writeValueAsString(Map.of(
                        ACCESS_TOKEN_HEADER, newAccessToken
                ))
        );
    }
}

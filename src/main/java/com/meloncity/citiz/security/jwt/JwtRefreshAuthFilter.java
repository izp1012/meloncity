package com.meloncity.citiz.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.ResponseDto;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.ProfileRepository;
import com.meloncity.citiz.util.CustomDateUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
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
            filterChain.doFilter(request, response);
            return;
        }

        try{
            String refreshToken = jwtTokenProvider.extractRefreshToken(request).orElseThrow(() -> new ResourceNotFoundException("RefreshToken", "Cookie", "Null"));
            TokenValidationResult valid = jwtTokenProvider.isValidJwt(refreshToken);

            if(valid != TokenValidationResult.SUCCESS){
                createReturnMsg(
                        response,
                        new ResponseDto<>(-1, null,"Invalid token.", CustomDateUtil.toStringFormat(LocalDateTime.now()))
                );
                log.info("Access Denied : RefreshToken이 유효하지 않습니다. serverName : {}", request.getServerName());

                return ;
            }else{
                checkRefreshTokenAndReIssueAccessToken(request, response, refreshToken);
            }

        }catch (ResourceNotFoundException e){
            createReturnMsg(
                    response,
                    new ResponseDto<>(-1, null,"There is no Refresh_Token.", CustomDateUtil.toStringFormat(LocalDateTime.now()))
            );

            return;
        }

    }

    private void checkRefreshTokenAndReIssueAccessToken(HttpServletRequest request, HttpServletResponse response, String refreshToken) throws IOException {
        //레디스에 저장된 토큰인지 아닌지 검증
        // 해당 토큰은 유출되었다고 판단 -> 추후 해당 세션을 막는 로직 필요

        String profileId = jwtTokenProvider.getSubject(refreshToken);
        Optional<Profile> profile = profileRepository.findByEmail(profileId);

        if(profile.isEmpty()){
            log.info("Access Denied : The user ID is invalid. ID : {}", profileId);
            createReturnMsg(
                    response,
                    new ResponseDto<>(-1, null,"The user ID is invalid.", CustomDateUtil.toStringFormat(LocalDateTime.now()))
            );
            return;
        }else if(!jwtTokenProvider.chkRefreshToken(profileId, refreshToken)){
            // 레디스에 있는 토큰과 동일한지 확인
            log.info("Access Denied : Invalid token.");
            createReturnMsg(
                    response,
                    new ResponseDto<>(-1, null,"Invalid token.", CustomDateUtil.toStringFormat(LocalDateTime.now()))
            );
            return;
        }

        List<String> roles = Collections.singletonList(profile.get().getRole());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        String newAccessToken = jwtTokenProvider.createToken(profileId, roles);
        String reIssueRefreshToken = jwtTokenProvider.reIssueRefreshToken(profileId, roles);

        ResponseCookie cookie = jwtTokenProvider.createCookie(reIssueRefreshToken);
        response.addHeader("Set-Cookie", cookie.toString());

        createReturnMsg(
                response,
                new ResponseDto<>(1, Map.of(ACCESS_TOKEN_HEADER, newAccessToken),"Token reissue success", CustomDateUtil.toStringFormat(LocalDateTime.now()))
        );
    }

    private void createReturnMsg(HttpServletResponse response, ResponseDto data) throws IOException{
        new ObjectMapper().writeValue(
                response.getWriter(),
                data
        );
    }
}

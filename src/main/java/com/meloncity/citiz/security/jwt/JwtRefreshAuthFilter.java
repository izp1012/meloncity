package com.meloncity.citiz.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.ResponseDto;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.ProfileRepository;
import com.meloncity.citiz.util.CustomDateUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
                        request,
                        response,
                        new ResponseDto<>(-1, valid,"Invalid token.", CustomDateUtil.toStringFormat(LocalDateTime.now())),
                        HttpServletResponse.SC_UNAUTHORIZED
                );
                log.info("Access Denied : RefreshToken이 유효하지 않습니다. serverName : {}", request.getServerName());

            }else{
                checkRefreshTokenAndReIssueAccessToken(request, response, refreshToken);
            }

            return;
        }catch (ResourceNotFoundException e){
            createReturnMsg(
                    request,
                    response,
                    new ResponseDto<>(-1, null,"There is no Refresh_Token.", CustomDateUtil.toStringFormat(LocalDateTime.now())),
                    HttpServletResponse.SC_UNAUTHORIZED
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
                    request,
                    response,
                    new ResponseDto<>(-1, TokenValidationResult.INVALID_TOKEN,"The user ID is invalid.", CustomDateUtil.toStringFormat(LocalDateTime.now())),
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }else if(!jwtTokenProvider.chkRefreshToken(profileId, refreshToken)){
            // 레디스에 있는 토큰과 동일한지 확인
            log.info("Access Denied : Invalid token.");
            createReturnMsg(
                    request,
                    response,
                    new ResponseDto<>(-1, TokenValidationResult.INVALID_TOKEN,"Invalid token.", CustomDateUtil.toStringFormat(LocalDateTime.now())),
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }else{
            List<String> roles = Collections.singletonList(profile.get().getRole() == null ? "ROLE_USER": profile.get().getRole());

            String newAccessToken = jwtTokenProvider.createToken(profileId, roles);
            jwtTokenProvider.reIssueRefreshToken(profileId, roles, response);

            ResponseDto data = new ResponseDto<>(1, newAccessToken,"Token reissue success", CustomDateUtil.toStringFormat(LocalDateTime.now()));
            createReturnMsg(request, response, data, HttpServletResponse.SC_OK);
        }
    }

    private void createReturnMsg(HttpServletRequest request, HttpServletResponse response, ResponseDto data, int status) throws IOException{
        jwtTokenProvider.basicResponseSet(request, response, status);

        String json = new ObjectMapper().writeValueAsString(data);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        response.setContentLength(jsonBytes.length);

        ServletOutputStream out = response.getOutputStream();
        out.write(jsonBytes);
        out.flush();
    }
}

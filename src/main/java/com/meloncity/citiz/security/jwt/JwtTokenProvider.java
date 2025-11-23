package com.meloncity.citiz.security.jwt;

import com.meloncity.citiz.dao.RedisJwtDao;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.CustomUserDetails;
import com.meloncity.citiz.dto.RedisJwtDto;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.ProfileRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

// JwtTokenProvider.java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.audience}") private String AUDIENCE;
    @Value("${spring.profiles.active}") private String PROFILES_ACTIVE;
    @Value("${jwt.access.expiration}") private long expiration; // seconds
    @Value("${jwt.issuer}") private String issuer;
    @Value("${jwt.refresh.expiration}") private long REFRESH_EXPIRATION;
    @Value("${jwt.refresh.header}") private String REFRESH_HEADER;

    private final RedisJwtDao redisJwtDao;
    private final ProfileRepository profileRepository;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)          // email
                .setIssuer(issuer)
                .setAudience(AUDIENCE)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expiration)))
                .claim("roles", roles)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpirationSeconds() { return expiration;}

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .setAllowedClockSkewSeconds(60) // 서버간 시간오차 1분 허용
                .requireIssuer(issuer) // 발급자 고정
                .build()
                .parseClaimsJws(token);
    }

    public String getSubject(String token) { return parse(token).getBody().getSubject(); }

    public List<String> getRoles(String token) {
        Object roles = parse(token).getBody().get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }


    // ##############REFRESH TOKEN ######################
    public String createRefreshToken(String subject, List<String> roles, HttpServletResponse response) {
        Instant now = Instant.now();
        String refreshToken = Jwts.builder()
                .setSubject(subject)          // email
                .setIssuer(issuer)
                .setAudience(AUDIENCE)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expiration)))
                .claim("roles", roles)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        RedisJwtDto redisJwtDto = new RedisJwtDto(refreshToken, RedisJwtStatus.ACTIVE);
        redisJwtDao.setValues(REFRESH_HEADER + "_" + subject, redisJwtDto, Duration.ofDays(REFRESH_EXPIRATION));

        ResponseCookie cookie = createCookie(refreshToken);
        response.addHeader("Set-Cookie", cookie.toString());

        return refreshToken;
    }

    public boolean chkRefreshToken(String subject, String refreshToken) {
        LinkedHashMap redisjwtDto = (LinkedHashMap)redisJwtDao.getValues(REFRESH_HEADER + "_" + subject);
        Boolean result = refreshToken.equals(redisjwtDto.get("refreshToken")) && RedisJwtStatus.ACTIVE == redisjwtDto.get("status");

        if(!result){
            // 문제가 있는 토큰으로 요청시 해당 토큰 상태(ACTIVE -> REVOKED) 변경
            redisjwtDto.put("status", RedisJwtStatus.REVOKED);
            redisJwtDao.setValues(REFRESH_HEADER + "_" + subject, redisjwtDto);
        }

        return result;
    }

    public String reIssueRefreshToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        String refreshToken = Jwts.builder()
                .setSubject(subject)          // email
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(REFRESH_EXPIRATION, ChronoUnit.DAYS)))
                .claim("roles", roles)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        RedisJwtDto redisJwtDto = new RedisJwtDto(refreshToken, RedisJwtStatus.ACTIVE);
        redisJwtDao.setValues(REFRESH_HEADER + "_" + subject, redisJwtDto, Duration.ofDays(REFRESH_EXPIRATION));

        return refreshToken;
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = "";

        for(Cookie cookie : cookies){
            if(REFRESH_HEADER.equals(cookie.getName())){
                token = cookie.getValue();
            }
        }
        
        if(!"".equals(token)){
            return Optional.of(token);
        }
        return Optional.empty();
    }

    public TokenValidationResult isValidJwt(String refreshToken) {
        try {
            // JWT 파싱 및 서명/먄료여부 확인
            // 추후 레디스에 있는지도 확인 -> 추가 예정
            Jws<Claims> claimsJws = parse(refreshToken);
            Claims payload = claimsJws.getBody();

            if (!payload.getAudience().contains(AUDIENCE)) {
                return TokenValidationResult.AUDIENCE_INVALID;
            }

            return TokenValidationResult.SUCCESS;

        } catch (ExpiredJwtException e) {
            return TokenValidationResult.EXPIRED;
        } catch (UnsupportedJwtException e) {
            return TokenValidationResult.UNSUPPORTED;
        } catch (MalformedJwtException e) {
            return TokenValidationResult.MALFORMED;
        } catch (JwtException e) {
            return TokenValidationResult.INVALID_SIGNATURE;
        } catch (IllegalArgumentException e) {
            return TokenValidationResult.EMPTY_OR_NULL;
        }
    }

    public ResponseCookie createCookie (String refreshToken){
        if("local".equals(PROFILES_ACTIVE)){
            return setCookieForLocal(refreshToken);
        }else{
            return setCookieForProd(refreshToken);
        }
    }

    private ResponseCookie setCookieForLocal(String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_HEADER, refreshToken)
                .path("/") // 모든 곳에서 쿠키열람이 가능하도록 설정
                .maxAge((int)Duration.ofDays(REFRESH_EXPIRATION).getSeconds()) //쿠키 만료시간 day
                .build();

        return cookie;
    }

    private ResponseCookie setCookieForProd(String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_HEADER, refreshToken)
                .httpOnly(true)  //httponly 옵션 설정
                .secure(true) //https 옵션 설정
                .path("/") // 모든 곳에서 쿠키열람이 가능하도록 설정
                .sameSite("Strict") // 쿠키 전송 조건 설정 Strict 또는 Lax 사용
                .maxAge((int)Duration.ofDays(REFRESH_EXPIRATION).getSeconds()) //쿠키 만료시간 day
                .build();

        return cookie;
    }

    public CustomUserDetails getUserDetails(String email){
        Profile profile = profileRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Profile", "E-mail", email));

        return CustomUserDetails.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .username(profile.getName())
                //.authorities(Collections.singletonList(new SimpleGrantedAuthority(profile.getRole())))
                .build();
    }
}

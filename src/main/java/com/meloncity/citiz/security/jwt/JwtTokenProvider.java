package com.meloncity.citiz.security.jwt;

import com.meloncity.citiz.config.redis.RedisJwtDao;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.repository.ProfileRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

        redisJwtDao.setValues(REFRESH_HEADER + "_" + subject, refreshToken, Duration.ofDays(REFRESH_EXPIRATION));

        Cookie cookie = createCookie(refreshToken);
        response.addCookie(cookie);

        return refreshToken;
    }

    public boolean chkRefreshToken(String subject, String refreshToken) {
        return refreshToken.equals(redisJwtDao.getValues(REFRESH_HEADER + "_" + subject).toString());
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

        redisJwtDao.setValues(REFRESH_HEADER + "_" + subject, refreshToken, Duration.ofDays(REFRESH_EXPIRATION));

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

    public Cookie createCookie (String refreshToken){
        if("local".equals(PROFILES_ACTIVE)){
            return setCookieForLocal(refreshToken);
        }else{
            return setCookieForProd(refreshToken);
        }
    }

    private Cookie setCookieForLocal(String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_HEADER, refreshToken);
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        cookie.setMaxAge((int)Duration.ofDays(REFRESH_EXPIRATION).getSeconds()); //쿠키 만료시간 day

        return cookie;
    }

    private Cookie setCookieForProd(String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_HEADER, refreshToken);
        cookie.setHttpOnly(true);  //httponly 옵션 설정
        cookie.setSecure(true); //https 옵션 설정
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        cookie.setMaxAge((int)Duration.ofDays(REFRESH_EXPIRATION).getSeconds()); //쿠키 만료시간 day

        return cookie;
    }
}

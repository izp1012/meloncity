package com.meloncity.citiz.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meloncity.citiz.dto.CustomUserDetails;
import com.meloncity.citiz.dto.ResponseDto;
import com.meloncity.citiz.util.CustomDateUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

// JwtAuthFilter.java
@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = resolveFromHeader(request); // Authorization: Bearer ...
        if (token != null) {
            TokenValidationResult valid = jwtTokenProvider.isValidJwt(token);

            if(valid != TokenValidationResult.SUCCESS){
                String origin = request.getHeader("Origin");
                if (origin != null) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                }
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                String json = new ObjectMapper().writeValueAsString(
                        new ResponseDto<>(-1, valid, "Invalid token.", CustomDateUtil.toStringFormat(LocalDateTime.now()))
                );
                byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

                response.setContentLength(jsonBytes.length);

                ServletOutputStream out = response.getOutputStream();
                out.write(jsonBytes);
                out.flush();

                log.info("Access Denied : Token이 유효하지 않습니다. serverName : {}", request.getServerName());

                return ;
            }

            var claims = jwtTokenProvider.parse(token).getBody();
            String subject = claims.getSubject();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.getOrDefault("roles", List.of("ROLE_USER"));

            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            CustomUserDetails customUserDetails = jwtTokenProvider.getUserDetails(subject);

            var authentication =
                    new UsernamePasswordAuthenticationToken(customUserDetails,null, authorities);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }

    private String resolveFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 다음부터 자르기
        }
        return null;
    }
}
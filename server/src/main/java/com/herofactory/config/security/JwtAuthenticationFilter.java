package com.herofactory.config.security;

import com.herofactory.login.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String accessToken = getJwtFromRequest(request);

            if (!StringUtils.hasText(accessToken)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token is missing", "token-missing");
                return;
            }
            try {
                // 토큰 검증
                if (jwtTokenProvider.validateToken(accessToken)) {
                    if (tokenService.isLogin(accessToken)) {
                        Long userId = jwtTokenProvider.getUserId(accessToken);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "User is logged out", "logged-out");
                        return;
                    }
                }
            } catch (ExpiredJwtException e) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Access token has expired", "token-expired");
                return;
            } catch (JwtException e) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid access token", "invalid-token");
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT Authentication error", e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication failed", "auth-failed");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message, String error)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Auth-Error", error);

        String jsonResponse = String.format(
                "{\"status\": %d, \"message\": \"%s\", \"error\": \"%s\"}",
                status.value(),
                message,
                error
        );
        response.getWriter().write(jsonResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/kakao/") ||
                path.startsWith("/test") ||
                path.startsWith("/images/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/auth/reissue") ||
                path.startsWith("/v3/api-docs");

    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
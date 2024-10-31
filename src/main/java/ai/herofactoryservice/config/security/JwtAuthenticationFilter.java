package ai.herofactoryservice.config.security;

import ai.herofactoryservice.login.service.TokenService;
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

            if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)) {
                if (tokenService.isLogin(accessToken)) {
                    Long userId = jwtTokenProvider.getUserId(accessToken);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 토큰은 유효하지만 로그아웃된 상태
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setHeader("X-Auth-Error", "logged-out");
                    response.getWriter().write("User is logged out");
                    return;
                }
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setHeader("X-Auth-Error", "token-expired");
                response.getWriter().write("Access token expired");
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT Authentication error", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("X-Auth-Error", "auth-failed");
            response.getWriter().write("Authentication failed");
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
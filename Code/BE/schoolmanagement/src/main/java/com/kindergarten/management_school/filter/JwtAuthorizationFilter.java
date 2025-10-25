package com.kindergarten.management_school.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String requestURI = request.getRequestURI();

        // Bỏ qua authorization check cho các endpoint auth
        if (isAuthEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                String path = request.getServletPath();

                if (path.startsWith("/employee") || requestURI.contains("/employee")) {
                    if (authentication.getAuthorities().stream()
                            .noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE") ||
                                    a.getAuthority().equals("ROLE_ADMIN"))) {
                        throw new AccessDeniedException("Access Denied for EMPLOYEE area");
                    }
                }
                else if (path.startsWith("/admin") || requestURI.contains("/admin")) {
                    if (authentication.getAuthorities().stream()
                            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        throw new AccessDeniedException("Access Denied for ADMIN area");
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (AccessDeniedException e) {
            SecurityContextHolder.clearContext();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Từ chối truy cập\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Lỗi xác thực\",\"message\":\"Xác thực thất bại\"}");
        }
    }

    private boolean isAuthEndpoint(String requestURI) {
        return requestURI.startsWith("/api/v1/auth/") ||
                requestURI.contains("/auth/") ||
                requestURI.equals("/api/v1/auth") ||
                requestURI.equals("/v1/auth") ||
                requestURI.endsWith("/auth/register") ||
                requestURI.endsWith("/auth/login") ||
                requestURI.endsWith("/auth/refresh") ||
                requestURI.endsWith("/auth/logout");
    }

    // Bỏ qua filter cho các request OPTIONS (CORS preflight)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
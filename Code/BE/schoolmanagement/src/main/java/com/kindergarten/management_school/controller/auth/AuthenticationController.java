package com.kindergarten.management_school.controller.auth;

import com.kindergarten.management_school.dto.request.*;
import com.kindergarten.management_school.dto.response.AuthenticationResponse;
import com.kindergarten.management_school.exception.RefreshTokenException;
import com.kindergarten.management_school.exception.RegisterException;
import com.kindergarten.management_school.service.authentication.AuthenticationService;
import com.kindergarten.management_school.service.jwt.JwtService;
import com.kindergarten.management_school.utils.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register/parent")
    public ResponseEntity<String> createParent(@RequestBody ParentRequest request) throws RegisterException {
        String result = authenticationService.createParent(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<String> createTeacher(@RequestBody TeacherRequest request) throws RegisterException {
        String result = authenticationService.createTeacher(request);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request,
                                                        HttpServletResponse response) {
        AuthenticationResponse authResponse = authenticationService.authenticate(request);

        cookieUtil.generatorTokenCookie(response, authResponse);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request,
                                                               HttpServletResponse response) throws RefreshTokenException {
        AuthenticationResponse newAuth = authenticationService.refreshToken(request);

        cookieUtil.saveAccessTokenCookie(response, newAuth);

        return ResponseEntity.ok(newAuth);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws RuntimeException {
        String refreshToken = getRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            jwtService.revokeRefreshToken(refreshToken);
        } else {
            throw new RuntimeException("Refresh token không có");
        }

        cookieUtil.removeCookies(response);
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

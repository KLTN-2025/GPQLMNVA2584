package com.kindergarten.management_school.service.authentication;

import com.kindergarten.management_school.dto.request.*;
import com.kindergarten.management_school.dto.response.AuthenticationResponse;
import com.kindergarten.management_school.exception.*;

public interface AuthenticationService {
    String createParent(ParentRequest request) throws RegisterException;

    String createTeacher(TeacherRequest request) throws RegisterException;

    AuthenticationResponse authenticate(LoginRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request) throws RefreshTokenException;

    String requestChangePassword(String email) throws AccountException;

    String changePassword(String email, String code, String newPassword) throws AccountException;
}

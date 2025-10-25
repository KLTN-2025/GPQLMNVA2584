package com.kindergarten.management_school.service.authentication;

import com.kindergarten.management_school.dto.request.*;
import com.kindergarten.management_school.dto.response.AuthenticationResponse;
import com.kindergarten.management_school.entity.*;
import com.kindergarten.management_school.exception.*;
import com.kindergarten.management_school.repository.*;
import com.kindergarten.management_school.service.email.EmailService;
import com.kindergarten.management_school.service.jwt.JwtService;
import com.kindergarten.management_school.service.redis.RedisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final PasswordEncoder encoder;
    private final RoleRepository roleRepo;
    private final AccountRepository accountRepo;
    private final ParentsRepository parentsRepo;
    private final TeacherRepository teacherRepo;
    private final StudentsRepository studentRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final WhiteListRepository whiteListRepo;
    private final BlackListRepository blackListRepo;
    private final RedisService redisService;
    private final EmailService emailService;

    @Override
    @Transactional
    public String createParent(ParentRequest request) throws RegisterException {
        try {
            Role role = roleRepo.findByName("PARENT")
                    .orElseThrow(() -> new RegisterException("Không tìm thấy vai trò PHỤ HUYNH."));

            if (accountRepo.existsByUsername(request.getUsername()))
                throw new RegisterException("Tên đăng nhập đã tồn tại!");
            if (accountRepo.existsByEmail(request.getEmail()))
                throw new RegisterException("Email đã được sử dụng!");
            if (accountRepo.existsByPhone(request.getPhone()))
                throw new RegisterException("Số điện thoại đã được sử dụng!");

            Account account = Account.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(encoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .gender(request.getGender())
                    .avatarUrl("https://i.postimg.cc/pVs3qTMy/image.png")
                    .role(role)
                    .isBlocked(false)
                    .build();

            Parents parent = Parents.builder()
                    .username(account.getUsername())
                    .email(account.getEmail())
                    .password(account.getPassword())
                    .fullName(account.getFullName())
                    .phone(account.getPhone())
                    .gender(account.getGender())
                    .dateOfBirth(request.getDateOfBirth())
                    .avatarUrl(account.getAvatarUrl())
                    .role(account.getRole())
                    .isBlocked(account.getIsBlocked())
                    .occupation(request.getOccupation())
                    .relationship(request.getRelationship())
                    .emergencyContact(request.getEmergencyContact())
                    .additionalPhone(request.getAdditionalPhone())
                    .build();

            parentsRepo.save(parent);


            return "Đăng ký tài khoản phụ huynh thành công!";
        } catch (DataIntegrityViolationException e) {
            throw new RegisterException("Lỗi dữ liệu khi đăng ký.");
        }
    }


    @Override
    @Transactional
    public String createTeacher(TeacherRequest request) throws RegisterException {
        try {
            Role role = roleRepo.findByName("TEACHER")
                    .orElseThrow(() -> new RegisterException("Không tìm thấy vai trò GIÁO VIÊN."));

            // Kiểm tra tồn tại
            if (accountRepo.existsByUsername(request.getUsername()))
                throw new RegisterException("Tên đăng nhập đã tồn tại!");
            if (accountRepo.existsByEmail(request.getEmail()))
                throw new RegisterException("Email đã được sử dụng!");
            if (accountRepo.existsByPhone(request.getPhone()))
                throw new RegisterException("Số điện thoại đã được sử dụng!");

            // 🧩 Tạo Teacher trực tiếp
            Teacher teacher = Teacher.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(encoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .gender(request.getGender())
                    .dateOfBirth(request.getDateOfBirth())
                    .avatarUrl("https://i.postimg.cc/pVs3qTMy/image.png") // Avatar mặc định
                    .role(role)
                    .isBlocked(false)
                    .employeeCode(generateEmployeeCode()) // Tạo mã nhân viên tự động
                    .specialization(request.getSpecialization())
                    .joinDate(LocalDateTime.now())
                    .emergencyContact(request.getEmergencyContact())
                    .build();

            Teacher savedTeacher = teacherRepo.save(teacher);

            return "Đăng ký tài khoản giáo viên thành công! Mã nhân viên: " + savedTeacher.getEmployeeCode();

        } catch (Exception e) {
            throw new RegisterException("Lỗi hệ thống khi đăng ký giáo viên: " + e.getMessage());
        }
    }

    @Override
    public AuthenticationResponse authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Kiểm tra chặn tài khoản
        if (userDetails instanceof Account account && Boolean.TRUE.equals(account.getIsBlocked())) {
            throw new AccountBlockedException("Tài khoản của bạn đã bị khóa!");
        }

        // Kiểm tra xem đã đăng nhập nơi khác chưa (white list)
        if (jwtService.isTokenInWhiteList(userDetails.getUsername())) {
            throw new AlreadyLoggedInException("Tài khoản đang đăng nhập ở thiết bị khác!");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateAndSaveRefreshToken(userDetails);

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));

        return AuthenticationResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(userDetails.getUsername())
                .role(role)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws RefreshTokenException {
        String newAccessToken = jwtService.generateNewAccessTokenFromRefreshToken(request.getRefreshToken());
        String username = jwtService.extractUsername(request.getRefreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .username(username)
                .role(role)
                .build();
    }

    @Override
    public String requestChangePassword(String email) throws AccountException {
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AccountException("Email không tồn tại trong hệ thống."));

        // Giới hạn yêu cầu OTP
        if (redisService.getOTPRequestCount(email) >= 3) {
            throw new AccountException("Bạn đã yêu cầu quá nhiều lần. Thử lại sau 30 phút.");
        }

        String code = generateRandomCode(6);
        redisService.saveOTP(email, code, 10);
        redisService.incrementOTPRequestCount(email);

        emailService.sendPasswordResetEmail(email, account.getFullName(), code);

        return "Mã OTP đã được gửi tới email của bạn.";
    }

    @Override
    public String changePassword(String email, String code, String newPassword) throws AccountException {
        String savedCode = redisService.getOTP(email);
        if (savedCode == null)
            throw new AccountException("Mã OTP đã hết hạn hoặc không tồn tại.");

        if (!savedCode.equals(code))
            throw new AccountException("Mã OTP không đúng.");

        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AccountException("Không tìm thấy tài khoản tương ứng."));

        if (encoder.matches(newPassword, account.getPassword()))
            throw new AccountException("Mật khẩu mới không được trùng với mật khẩu cũ.");

        account.setPassword(encoder.encode(newPassword));
        accountRepo.save(account);

        redisService.deleteOTP(email);
        redisService.deleteOTPRequestCount(email);

        return "Đổi mật khẩu thành công!";
    }

    private String generateRandomCode(int length) {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, length)
                .toUpperCase();
    }

    /** GV + năm + tháng + số thứ tự */
    private String generateEmployeeCode() {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));

        String latestCode = teacherRepo.findLatestEmployeeCodeByMonth(yearMonth);
        int nextNumber = 1;

        if (latestCode != null && latestCode.startsWith("GV" + yearMonth)) {
            try {
                String numberPart = latestCode.substring(2 + yearMonth.length());
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException e) {
                nextNumber = 1;
            }
        }

        return String.format("GV%s%03d", yearMonth, nextNumber);
    }
}

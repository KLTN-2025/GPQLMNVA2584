package com.kindergarten.management_school.config;

import com.kindergarten.management_school.entity.Account;
import com.kindergarten.management_school.entity.Students;
import com.kindergarten.management_school.entity.Parents;
import com.kindergarten.management_school.entity.Teacher;
import com.kindergarten.management_school.repository.AccountRepository;
import com.kindergarten.management_school.repository.StudentsRepository;
import com.kindergarten.management_school.repository.ParentsRepository;
import com.kindergarten.management_school.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AccountRepository accountRepository;
    private final StudentsRepository studentsRepository;
    private final TeacherRepository teachersRepository;
    private final ParentsRepository parentsRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Kiểm tra trong Teachers
            Optional<Teacher> teacher = teachersRepository.findByAccount_Username(username);
            if (teacher.isPresent()) {
                return teacher.get();
            }

            // Kiểm tra trong Students
            Optional<Students> student = studentsRepository.findByAccount_Username(username);
            if (student.isPresent()) {
                return student.get();
            }

            // Kiểm tra trong Parents
            Optional<Parents> parent = parentsRepository.findByAccount_Username(username);
            if (parent.isPresent()) {
                return parent.get();
            }

            // Cuối cùng kiểm tra trực tiếp trong bảng Account
            return accountRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
        };
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

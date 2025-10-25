package com.kindergarten.management_school.config;

import com.kindergarten.management_school.entity.Account;
import com.kindergarten.management_school.entity.Role;
import com.kindergarten.management_school.repository.AccountRepository;
import com.kindergarten.management_school.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ADMIN");
                    return roleRepository.save(newRole);
                });

        if (!accountRepository.existsByUsername("admin")) {
            Account admin = new Account();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrator");
            admin.setPhone("0999999999");
            admin.setGender("Nam");
            admin.setRole(adminRole);
            admin.setIsBlocked(false);
            accountRepository.save(admin);
            System.out.println("✅ Admin account created successfully: username=admin, password=admin123");
        } else {
            System.out.println("ℹ️ Admin account already exists. Skipped creation.");
        }
    }
}

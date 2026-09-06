package com.urigym.config;

import com.urigym.domain.user.AppRole;
import com.urigym.domain.user.User;
import com.urigym.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds test accounts for the {@code local} profile only — no gyms, so the map starts
 * empty and shows only what's actually registered through the app.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "urigym123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = createUser("admin@urigym.com", "관리자", "010-0000-0001", AppRole.ADMIN);
        User owner = createUser("owner@urigym.com", "김관장", "010-0000-0002", AppRole.OWNER);
        User member = createUser("user@urigym.com", "이회원", "010-0000-0003", AppRole.USER);

        log.info("""

                ===== 로컬 시드 계정 (비밀번호: {}) =====
                  관리자 : {}
                  관장   : {}
                  일반   : {}
                ========================================
                """, DEFAULT_PASSWORD, admin.getEmail(), owner.getEmail(), member.getEmail());
    }

    private User createUser(String email, String name, String phone, AppRole role) {
        return userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName(name)
                .phone(phone)
                .address("경기도 시흥시 월곶동")
                .role(role)
                .build());
    }
}

package com.urigym.config;

import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.GymRepository;
import com.urigym.domain.member.GymMember;
import com.urigym.domain.member.GymMemberRepository;
import com.urigym.domain.review.Review;
import com.urigym.domain.review.ReviewRepository;
import com.urigym.domain.user.AppRole;
import com.urigym.domain.user.User;
import com.urigym.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds a usable data set for the {@code local} profile only, so the admin and owner
 * screens have something to show without a manual setup pass.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "urigym123";

    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final GymMemberRepository gymMemberRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = createUser("admin@urigym.com", "관리자", "010-0000-0001", AppRole.ADMIN);
        User owner = createUser("owner@urigym.com", "김관장", "010-0000-0002", AppRole.OWNER);
        User member = createUser("user@urigym.com", "이회원", "010-0000-0003", AppRole.USER);

        Gym gym = gymRepository.save(Gym.builder()
                .name("파워짐 피트니스")
                .category("헬스장")
                .address("서울특별시 강남구 테헤란로 123")
                .description("최신 장비를 갖춘 24시간 헬스장입니다.")
                .phone("02-1234-5678")
                .lat(37.5008)
                .lng(127.0365)
                .priceMin(89000)
                .priceMax(159000)
                .rating(BigDecimal.valueOf(4.5))
                .reviewCount(1)
                .memberCount(1)
                .tags(new java.util.ArrayList<>(List.of("주차가능", "샤워실", "24시간")))
                .owner(owner)
                .build());

        Gym secondGym = gymRepository.save(Gym.builder()
                .name("바른 필라테스")
                .category("필라테스")
                .address("서울특별시 서초구 서초대로 45")
                .description("소수정예 필라테스 스튜디오.")
                .phone("02-2345-6789")
                .lat(37.4954)
                .lng(127.0281)
                .priceMin(150000)
                .priceMax(250000)
                .rating(BigDecimal.ZERO)
                .tags(new java.util.ArrayList<>(List.of("소수정예", "여성전용")))
                .owner(owner)
                .build());

        gymMemberRepository.save(GymMember.builder().gym(gym).user(member).build());

        reviewRepository.save(Review.builder()
                .gym(gym)
                .user(member)
                .rating(5)
                .content("시설이 깔끔하고 트레이너분들이 정말 친절합니다. 추천해요!")
                .build());

        log.info("""

                ===== 로컬 시드 계정 (비밀번호: {}) =====
                  관리자 : {}
                  관장   : {}
                  일반   : {}
                  체육관 : {} / {}
                ========================================
                """, DEFAULT_PASSWORD, admin.getEmail(), owner.getEmail(), member.getEmail(),
                gym.getName(), secondGym.getName());
    }

    private User createUser(String email, String name, String phone, AppRole role) {
        return userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName(name)
                .phone(phone)
                .address("서울특별시 강남구")
                .role(role)
                .build());
    }
}

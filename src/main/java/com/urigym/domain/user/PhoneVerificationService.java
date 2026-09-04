package com.urigym.domain.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ponytail: no SMS provider is wired up yet (needs a real account — Aligo/NHN Cloud/etc),
 * so this logs the code to the server console instead of texting it. Swap sendCode's body
 * for a real provider call when one is chosen; confirmCode and the User.phoneVerified flag
 * don't need to change.
 * <p>
 * Codes live in memory only (not the DB) since they're short-lived (5 min) and worthless
 * after a restart — a real provider would likely track delivery state differently anyway,
 * so persisting this mock shape isn't worth it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final Map<UUID, PendingCode> pendingCodes = new ConcurrentHashMap<>();

    public void sendCode(UUID userId, String phone) {
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        pendingCodes.put(userId, new PendingCode(phone, code, Instant.now().plus(CODE_TTL)));

        log.info("""

                ===== [개발용] 전화번호 인증번호 =====
                  대상 번호 : {}
                  인증번호  : {}
                  (SMS 연동 전까지는 실제 문자 대신 여기 로그로 확인합니다)
                =====================================
                """, phone, code);
    }

    @Transactional
    public void confirmCode(UUID userId, String phone, String code) {
        PendingCode pending = pendingCodes.get(userId);

        if (pending == null || !pending.phone().equals(phone) || Instant.now().isAfter(pending.expiresAt())) {
            throw new IllegalArgumentException("인증번호가 만료되었거나 요청 내역이 없습니다. 다시 요청해주세요.");
        }
        if (!pending.code().equals(code)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        pendingCodes.remove(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setPhone(phone);
        user.setPhoneVerified(true);
        userRepository.save(user);
    }

    private record PendingCode(String phone, String code, Instant expiresAt) {
    }
}

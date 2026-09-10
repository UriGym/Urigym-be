package com.urigym.domain.member;

import com.urigym.common.exception.DuplicateResourceException;
import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.attendance.Attendance;
import com.urigym.domain.attendance.AttendanceRepository;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.entity.GymResponse;
import com.urigym.domain.member.entity.GymMemberRequest;
import com.urigym.domain.member.entity.GymMemberResponse;
import com.urigym.domain.member.entity.MyMembershipResponse;
import com.urigym.domain.notification.NotificationService;
import com.urigym.domain.notification.NotificationType;
import com.urigym.domain.user.User;
import com.urigym.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymMemberService {

    private final GymMemberRepository gymMemberRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public List<GymMemberResponse> getMembersWithStats(UUID gymId) {
        Map<UUID, LocalDateTime> lastCheckIns = new HashMap<>();
        gymMemberRepository.findLastCheckInByGymId(gymId)
                .forEach(row -> lastCheckIns.put((UUID) row[0], (LocalDateTime) row[1]));

        Map<UUID, Long> attendanceCounts = new HashMap<>();
        gymMemberRepository.findAttendanceCountByGymId(gymId)
                .forEach(row -> attendanceCounts.put((UUID) row[0], (Long) row[1]));

        return gymMemberRepository.findByGymId(gymId).stream()
                .map(member -> GymMemberResponse.withStats(
                        member,
                        lastCheckIns.get(member.getId()),
                        attendanceCounts.getOrDefault(member.getId(), 0L)
                ))
                .sorted(Comparator.comparing(GymMemberResponse::getJoinedAt).reversed())
                .toList();
    }

    /** Members who have not checked in for at least {@code days}, never-attended included. */
    public List<GymMemberResponse> getAbsentMembers(UUID gymId, int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);

        return getMembersWithStats(gymId).stream()
                .filter(member -> member.getLastCheckInTime() == null
                        || member.getLastCheckInTime().isBefore(threshold))
                .sorted(Comparator.comparing(
                        GymMemberResponse::getLastCheckInTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
                .toList();
    }

    public List<GymMember> getMembersByIds(List<UUID> ids) {
        return gymMemberRepository.findByIdIn(ids);
    }

    public List<GymMember> getMembers(UUID gymId) {
        return gymMemberRepository.findByGymId(gymId);
    }

    /**
     * A user typically belongs to a handful of gyms, so per-membership attendance
     * queries (2 per gym) are fine here — unlike {@link #getMembersWithStats}, which
     * batches into 2 queries total because a gym can have 100+ members and per-member
     * queries there would be a real N+1.
     */
    public List<MyMembershipResponse> getMyMemberships(UUID userId) {
        return gymMemberRepository.findByUserId(userId).stream()
                .map(member -> MyMembershipResponse.builder()
                        .id(member.getId())
                        .gym(GymResponse.from(member.getGym()))
                        .status(member.getStatus())
                        .joinedAt(member.getJoinedAt())
                        .expiresAt(member.getExpiresAt())
                        .lastCheckInTime(attendanceRepository
                                .findTopByGymIdAndUserIdOrderByCheckInTimeDesc(member.getGym().getId(), userId)
                                .map(Attendance::getCheckInTime)
                                .orElse(null))
                        .attendanceCount(attendanceRepository.countByGymIdAndUserId(member.getGym().getId(), userId))
                        .build())
                .sorted(Comparator.comparing(MyMembershipResponse::getJoinedAt).reversed())
                .toList();
    }

    /**
     * Hard-deletes the membership row, same as the owner-side removeMember. Safe to do:
     * Attendance rows reference user+gym directly, not GymMember, so past visit history
     * survives cancellation — only future check-ins are blocked (checkIn requires an
     * active GymMember row).
     */
    @Transactional
    public void cancelOwnMembership(UUID memberId, UUID userId) {
        GymMember member = getMemberById(memberId);
        if (!member.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 회원권만 해지할 수 있습니다.");
        }

        Gym gym = member.getGym();
        gymMemberRepository.delete(member);
        gym.setMemberCount((int) gymMemberRepository.countByGymIdAndStatus(gym.getId(), "ACTIVE"));
    }

    public GymMember getMemberById(UUID id) {
        return gymMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym member not found with id: " + id));
    }

    @Transactional
    public GymMember addMember(Gym gym, GymMemberRequest request) {
        User user = userService.getUserByEmail(request.getUserEmail());

        if (gymMemberRepository.existsByGymIdAndUserId(gym.getId(), user.getId())) {
            throw new DuplicateResourceException("이미 등록된 관원입니다.");
        }

        // status는 항상 INVITED로 고정한다: 클라이언트가 보낸 값을 신뢰하면 owner가 API를
        // 직접 호출해 "ACTIVE"를 보내는 것으로 유저 동의(수락) 절차를 우회할 수 있다.
        GymMember member = gymMemberRepository.save(GymMember.builder()
                .gym(gym)
                .user(user)
                .status("INVITED")
                .expiresAt(request.getExpiresAt())
                .build());

        notificationService.notify(
                user,
                NotificationType.SYSTEM,
                "체육관 등록 초대가 도착했습니다.",
                gym.getName() + "에서 회원 등록을 초대했습니다. 마이페이지에서 수락하면 등록됩니다.",
                gym.getId()
        );

        return member;
    }

    /** Accepts a gym-initiated invite: INVITED -> ACTIVE, notifies the owner. */
    @Transactional
    public void acceptInvite(UUID memberId, UUID userId) {
        GymMember member = getMemberById(memberId);
        if (!member.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 초대만 수락할 수 있습니다.");
        }
        if (!"INVITED".equals(member.getStatus())) {
            throw new IllegalArgumentException("수락할 수 있는 초대 상태가 아닙니다.");
        }

        member.setStatus("ACTIVE");
        gymMemberRepository.save(member);

        Gym gym = member.getGym();
        gym.setMemberCount((int) gymMemberRepository.countByGymIdAndStatus(gym.getId(), "ACTIVE"));

        notificationService.notify(
                gym.getOwner(),
                NotificationType.SYSTEM,
                "초대를 수락했습니다.",
                member.getUser().getFullName() + "님이 초대를 수락했습니다.",
                gym.getId()
        );
    }

    /** Declines a gym-initiated invite: deletes the row, notifies the owner. */
    @Transactional
    public void declineInvite(UUID memberId, UUID userId) {
        GymMember member = getMemberById(memberId);
        if (!member.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 초대만 거절할 수 있습니다.");
        }
        if (!"INVITED".equals(member.getStatus())) {
            throw new IllegalArgumentException("거절할 수 있는 초대 상태가 아닙니다.");
        }

        Gym gym = member.getGym();
        User user = member.getUser();
        gymMemberRepository.delete(member);
        gym.setMemberCount((int) gymMemberRepository.countByGymIdAndStatus(gym.getId(), "ACTIVE"));

        notificationService.notify(
                gym.getOwner(),
                NotificationType.SYSTEM,
                "초대를 거절했습니다.",
                user.getFullName() + "님이 초대를 거절했습니다.",
                gym.getId()
        );
    }

    /** Free join request: creates a PENDING member row and notifies the gym's owner. */
    @Transactional
    public GymMember requestJoin(Gym gym, User user) {
        if (gym.getOwner() == null) {
            throw new IllegalArgumentException("관장이 지정되지 않은 체육관은 등록 신청할 수 없습니다.");
        }
        gymMemberRepository.findByGymIdAndUserId(gym.getId(), user.getId()).ifPresent(existing -> {
            throw new DuplicateResourceException(
                    "PENDING".equals(existing.getStatus()) ? "이미 등록 신청한 체육관입니다." : "이미 등록된 관원입니다.");
        });

        GymMember member = gymMemberRepository.save(GymMember.builder()
                .gym(gym)
                .user(user)
                .status("PENDING")
                .build());

        notificationService.notify(
                gym.getOwner(),
                NotificationType.SYSTEM,
                "새 등록 신청이 있습니다.",
                user.getFullName() + "님이 " + gym.getName() + " 등록을 신청했습니다.",
                gym.getId()
        );

        return member;
    }

    /** Approves a PENDING join request: PENDING → ACTIVE, notifies the requesting user. */
    @Transactional
    public GymMember approveMember(UUID memberId, UUID gymId, Gym gym) {
        GymMember member = getMemberOfGym(memberId, gymId);
        member.setStatus("ACTIVE");
        gym.setMemberCount((int) gymMemberRepository.countByGymIdAndStatus(gymId, "ACTIVE"));
        gymMemberRepository.save(member);

        notificationService.notify(
                member.getUser(),
                NotificationType.SYSTEM,
                "등록 신청이 승인되었습니다.",
                gym.getName() + " 등록이 승인되었습니다. 이제 출석 체크가 가능합니다.",
                gym.getId()
        );

        return member;
    }

    /** Rejects a join request: deletes the row, notifies the requesting user. */
    @Transactional
    public void rejectMember(UUID memberId, UUID gymId, Gym gym) {
        GymMember member = getMemberOfGym(memberId, gymId);
        User user = member.getUser();
        gymMemberRepository.delete(member);
        gym.setMemberCount((int) gymMemberRepository.countByGymIdAndStatus(gymId, "ACTIVE"));

        notificationService.notify(
                user,
                NotificationType.SYSTEM,
                "등록 신청이 거절되었습니다.",
                gym.getName() + " 등록 신청이 거절되었습니다.",
                gym.getId()
        );
    }

    @Transactional
    public GymMember updateMember(UUID memberId, UUID gymId, GymMemberRequest request) {
        GymMember member = getMemberOfGym(memberId, gymId);
        if (request.getStatus() != null) {
            member.setStatus(request.getStatus());
        }
        member.setExpiresAt(request.getExpiresAt());
        return gymMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(UUID memberId, UUID gymId, Gym gym) {
        gymMemberRepository.delete(getMemberOfGym(memberId, gymId));
        gym.setMemberCount((int) gymMemberRepository.countByGymIdAndStatus(gymId, "ACTIVE"));
    }

    private GymMember getMemberOfGym(UUID memberId, UUID gymId) {
        GymMember member = getMemberById(memberId);
        if (!member.getGym().getId().equals(gymId)) {
            throw new IllegalArgumentException("해당 체육관의 관원이 아닙니다.");
        }
        return member;
    }
}

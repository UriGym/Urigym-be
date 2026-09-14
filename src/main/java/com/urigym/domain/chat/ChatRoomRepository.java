package com.urigym.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findByGymIdAndInquirerId(UUID gymId, UUID inquirerId);

    /** Rooms where the current user is either the inquirer or the gym's owner, most recently active first. */
    @Query("""
            SELECT r FROM ChatRoom r
            WHERE r.inquirer.id = :userId OR r.gym.owner.id = :userId
            ORDER BY r.lastMessageAt DESC
            """)
    List<ChatRoom> findByGymOwnerIdOrInquirerId(@Param("userId") UUID userId);
}

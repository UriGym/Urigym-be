package com.urigym.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(UUID roomId);

    /**
     * One row per room: the message with the latest createdAt in that room, for the room-list preview.
     * ponytail: correlated subquery, fine at this scale (a handful of rooms per user); if a room ever
     * gets two messages with the exact same timestamp this can return both — negligible in practice
     * for human-typed chat messages.
     */
    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.room.id IN :roomIds
              AND m.createdAt = (SELECT MAX(m2.createdAt) FROM ChatMessage m2 WHERE m2.room.id = m.room.id)
            """)
    List<ChatMessage> findLatestMessagesByRoomIds(@Param("roomIds") List<UUID> roomIds);

    /** [roomId, unreadCount] pairs for messages in {@code roomIds} not sent by {@code viewerId}. */
    @Query("""
            SELECT m.room.id, COUNT(m)
            FROM ChatMessage m
            WHERE m.room.id IN :roomIds AND m.sender.id <> :viewerId AND m.isRead = false
            GROUP BY m.room.id
            """)
    List<Object[]> countUnreadByRoomIds(@Param("roomIds") List<UUID> roomIds, @Param("viewerId") UUID viewerId);
}

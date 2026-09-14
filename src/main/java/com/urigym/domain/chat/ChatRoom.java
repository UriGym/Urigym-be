package com.urigym.domain.chat;

import com.urigym.domain.gym.Gym;
import com.urigym.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 1:1 price-inquiry chat room between a gym's owner and an inquiring user.
 * No separate {@code owner_id} column — the owner side is always {@code gym.getOwner()};
 * gym ownership never changes after creation (see {@link Gym#owner}), so joining through
 * the gym is enough and avoids a duplicated, potentially stale reference.
 */
@Entity
@Table(name = "chat_rooms", uniqueConstraints = @UniqueConstraint(columnNames = {"gym_id", "inquirer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquirer_id", nullable = false)
    private User inquirer;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
}

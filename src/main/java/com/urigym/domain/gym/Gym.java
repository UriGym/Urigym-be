package com.urigym.domain.gym;

import com.urigym.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "gyms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gym {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String address;

    private String description;

    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_open")
    @Builder.Default
    private Boolean isOpen = true;

    private Double lat;
    private Double lng;

    @Column(name = "member_count")
    @Builder.Default
    private Integer memberCount = 0;

    @Column(precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "favorite_count")
    @Builder.Default
    private Integer favoriteCount = 0;

    @Column(name = "price_min")
    private Integer priceMin;

    @Column(name = "price_max")
    private Integer priceMax;

    @Column(name = "report_count")
    @Builder.Default
    private Integer reportCount = 0;

    /** Set by an admin to hide the gym from public listings until this moment passes. */
    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @ElementCollection
    @CollectionTable(name = "gym_tags", joinColumns = @JoinColumn(name = "gym_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isSuspended() {
        return suspendedUntil != null && suspendedUntil.isAfter(LocalDateTime.now());
    }
}

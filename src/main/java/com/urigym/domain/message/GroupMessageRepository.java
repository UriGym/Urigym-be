package com.urigym.domain.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, UUID> {

    Page<GroupMessage> findByGymIdOrderByCreatedAtDesc(UUID gymId, Pageable pageable);
}

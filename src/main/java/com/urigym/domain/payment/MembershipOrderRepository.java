package com.urigym.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipOrderRepository extends JpaRepository<MembershipOrder, UUID> {

    Optional<MembershipOrder> findByOrderId(String orderId);
}

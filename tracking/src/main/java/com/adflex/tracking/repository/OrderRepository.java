package com.adflex.tracking.repository;

import com.adflex.tracking.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByLeadIdOrderByCreatedAtDesc(String leadId);

    Optional<Order> findByPublicToken(UUID publicToken);
}
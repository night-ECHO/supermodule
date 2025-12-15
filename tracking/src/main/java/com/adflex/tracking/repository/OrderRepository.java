package com.adflex.tracking.repository;

import com.adflex.tracking.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
    java.util.List<Order> findByLeadIdOrderByCreatedAtDesc(String leadId);
}

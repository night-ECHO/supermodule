package com.adflex.tracking.repository;

import com.adflex.tracking.entity.LeadProgress;
import com.adflex.tracking.enums.MilestoneStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadProgressRepository extends JpaRepository<LeadProgress, String> {

    LeadProgress findByLeadIdAndMilestoneCode(String leadId, String milestoneCode);

    boolean existsByLeadIdAndMilestoneCode(String leadId, String milestoneCode);

    List<LeadProgress> findByLeadIdOrderByCreatedAtAsc(String leadId);
    List<LeadProgress> findByStatus(MilestoneStatus status);

}

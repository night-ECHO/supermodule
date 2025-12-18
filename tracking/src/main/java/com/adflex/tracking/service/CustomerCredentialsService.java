package com.adflex.tracking.service;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class CustomerCredentialsService {

    private final LeadRepository leadRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public CustomerCredentialsService(LeadRepository leadRepository, PasswordEncoder passwordEncoder) {
        this.leadRepository = leadRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Ensure tracking_token + access_code are present for customer portal access.
     * Idempotent: only generates missing fields.
     *
     * @return the plain access code if it was newly generated, otherwise null.
     */
    public String ensureCredentials(Lead lead) {
        boolean changed = false;
        String plainAccessCode = null;

        if (lead.getTrackingToken() == null) {
            lead.setTrackingToken(UUID.randomUUID());
            changed = true;
        }

        if (lead.getAccessCode() == null || lead.getAccessCode().isBlank()) {
            plainAccessCode = generateAccessCode();
            lead.setAccessCode(passwordEncoder.encode(plainAccessCode));
            changed = true;
        }

        if (changed) {
            leadRepository.save(lead);
        }

        return plainAccessCode;
    }

    /**
     * Reset the customer access code and return the new plain value.
     * This is intended for admin support flows (manual resend).
     */
    public String resetAccessCode(Lead lead) {
        if (lead.getTrackingToken() == null) {
            lead.setTrackingToken(UUID.randomUUID());
        }
        String plain = generateAccessCode();
        lead.setAccessCode(passwordEncoder.encode(plain));
        leadRepository.save(lead);
        return plain;
    }

    private String generateAccessCode() {
        int value = random.nextInt(900_000) + 100_000;
        return Integer.toString(value);
    }
}

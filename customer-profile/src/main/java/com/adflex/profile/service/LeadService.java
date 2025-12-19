package com.adflex.profile.service;

import com.adflex.profile.dto.request.LeadPayload;
import com.adflex.profile.entity.Lead;
import com.adflex.profile.entity.LeadStatus;
import com.adflex.profile.event.LeadCreatedEvent;
import com.adflex.profile.event.LeadDuplicateEvent;
import com.adflex.profile.repository.LeadRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Lead processIncomingLead(LeadPayload payload) {
        String rawPhone = payload.getSdt();
        String normalizedPhone = normalizePhoneNumber(rawPhone);

        // Tìm lead tồn tại theo sđt đã chuẩn hoá
        return leadRepository.findByPhone(normalizedPhone)
                .map(existing -> handleDuplicateLead(existing))
                .orElseGet(() -> createNewLead(payload, normalizedPhone));
    }


    private Lead handleDuplicateLead(Lead existing) {
        existing.setIsDuplicate(true);
        Lead saved = leadRepository.save(existing);


        eventPublisher.publishEvent(
                new LeadDuplicateEvent(
                        saved.getId().toString(),
                        saved.getPhone(),
                        saved.getFullName()
                )
        );


        return saved;
    }

    /**
     * Tạo lead mới:
     * - Map từ LeadPayload sang entity Lead
     * - Lưu DB
     * - Bắn event LeadCreatedEvent (notification layer sẽ gửi Telegram)
     */
    private Lead createNewLead(LeadPayload payload, String normalizedPhone) {
        Lead lead = new Lead();
        lead.setMbRefId(payload.getMbRefId());
        lead.setFullName(payload.getTenNguoiGui());
        lead.setPhone(normalizedPhone);
        lead.setEmail(payload.getEmail());
        lead.setBusinessAddress(payload.getDiaChiDn());
        lead.setIndustryNeeds(payload.getNhuCau());
        lead.setCharterCapital(payload.getCharterCapital());
        lead.setIsDuplicate(false);
        lead.setStatus(LeadStatus.NEW);
        // Org mặc định ULTRA (như mô tả)
        lead.setAssignedToOrg("ULTRA");

        // Gom các option tên DN vào list
        List<String> nameOptions = new ArrayList<>();
        if (payload.getTenDnOption1() != null && !payload.getTenDnOption1().isBlank()) {
            nameOptions.add(payload.getTenDnOption1());
        }
        if (payload.getTenDnOption2() != null && !payload.getTenDnOption2().isBlank()) {
            nameOptions.add(payload.getTenDnOption2());
        }
        if (payload.getTenDnOption3() != null && !payload.getTenDnOption3().isBlank()) {
            nameOptions.add(payload.getTenDnOption3());
        }
        if (payload.getTenDnOption4() != null && !payload.getTenDnOption4().isBlank()) {
            nameOptions.add(payload.getTenDnOption4());
        }go
        if (payload.getTenDnOption5() != null && !payload.getTenDnOption5().isBlank()) {
            nameOptions.add(payload.getTenDnOption5());
        }
        lead.setBusinessNameOptions(nameOptions);

        Lead saved = leadRepository.save(lead);

        // Bắn event lead mới cho notification
        eventPublisher.publishEvent(
                new LeadCreatedEvent(
                        saved.getId().toString(),
                        saved.getPhone(),
                        saved.getFullName(),
                        saved.getEmail()
                )
        );

        return saved;
    }

    /**
     * Chuẩn hóa số điện thoại: bỏ space, -, (), chuyển +84 -> 0
     */
    public String normalizePhoneNumber(String phone) {
        if (phone == null) return null;

        String cleaned = phone.trim();
        // Bỏ mọi ký tự không phải số hoặc +
        cleaned = cleaned.replaceAll("[^\\d+]", "");

        if (cleaned.startsWith("+84")) {
            cleaned = "0" + cleaned.substring(3);
        } else if (cleaned.startsWith("84") && !cleaned.startsWith("840")) {
            cleaned = "0" + cleaned.substring(2);
        }
        // Nếu còn ký tự + ở giữa thì bỏ
        cleaned = cleaned.replace("+", "");
        return cleaned;
    }
}

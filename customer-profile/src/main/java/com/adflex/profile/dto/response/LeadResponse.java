package com.adflex.profile.dto.response;

import com.adflex.profile.entity.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LeadResponse {
    private UUID id;
    private LeadStatus status;
}

package com.adflex.profile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookRequest {

    @JsonProperty("data")
    @Valid
    @NotNull(message = "data không được null")
    private LeadPayload data;
}

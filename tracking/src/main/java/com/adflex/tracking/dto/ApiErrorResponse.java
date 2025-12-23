package com.adflex.tracking.dto;

import com.adflex.tracking.config.DateTimeConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}

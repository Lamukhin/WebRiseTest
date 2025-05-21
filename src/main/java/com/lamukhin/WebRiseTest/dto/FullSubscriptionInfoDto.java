package com.lamukhin.WebRiseTest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FullSubscriptionInfoDto(
        Integer id,
        UUID userId,
        String serviceName,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}

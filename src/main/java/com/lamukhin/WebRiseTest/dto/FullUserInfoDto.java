package com.lamukhin.WebRiseTest.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FullUserInfoDto(
        UUID id,
        String userName,
        String email,
        LocalDateTime registrationTime,
        int subscriptionAmount
) {
}

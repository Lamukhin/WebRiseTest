package com.lamukhin.WebRiseTest.dto;


public record UpdateUserDataDto(
        String userName,
        String email,
        String newEmail,
        int subscriptionAmount
) {
}

package com.lamukhin.WebRiseTest.dto;


public record UserInfoDto(
        String userName,
        String email,
        int subscriptionAmount
) {
}

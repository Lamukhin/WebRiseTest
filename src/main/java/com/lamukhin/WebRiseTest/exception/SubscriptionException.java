package com.lamukhin.WebRiseTest.exception;


import lombok.Getter;

@Getter
public class SubscriptionException extends RuntimeException {

    private final String message;

    public SubscriptionException(String message) {
        this.message = message;
    }

    public SubscriptionException() {
        this.message = "";
    }
}

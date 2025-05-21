package com.lamukhin.WebRiseTest.exception;


import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {

    private final String message;

    public UserNotFoundException(String message) {
        this.message = message;
    }

    public UserNotFoundException() {
        this.message = "";
    }
}

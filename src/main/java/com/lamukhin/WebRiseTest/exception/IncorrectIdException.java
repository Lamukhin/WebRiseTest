package com.lamukhin.WebRiseTest.exception;


import lombok.Getter;

@Getter
public class IncorrectIdException extends RuntimeException {

    private final String message;

    public IncorrectIdException(String message) {
        this.message = message;
    }

    public IncorrectIdException() {
        this.message = "";
    }
}

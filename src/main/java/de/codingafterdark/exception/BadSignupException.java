package de.codingafterdark.exception;

public class BadSignupException extends RuntimeException {
    public BadSignupException(String message) {
        super(message);
    }
}

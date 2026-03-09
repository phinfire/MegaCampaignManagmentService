package de.codingafterdark.exception;

public class NoUserInJWTException extends RuntimeException {
    public NoUserInJWTException() {
        super("No user ID found in JWT token");
    }
}

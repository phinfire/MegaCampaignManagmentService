package de.codingafterdark.exception;

public class NotAnAdminException extends RuntimeException {
    public NotAnAdminException() {
        super("User is not an admin.");
    }
}

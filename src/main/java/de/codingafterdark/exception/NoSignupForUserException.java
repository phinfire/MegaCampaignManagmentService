package de.codingafterdark.exception;

public class NoSignupForUserException extends RuntimeException {
    public NoSignupForUserException(Long campaignId, String userId) {
        super("No signup found for user '" + userId + "' in campaign " + campaignId);
    }
}
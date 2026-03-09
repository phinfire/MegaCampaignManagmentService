package de.codingafterdark.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthenticationService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Extracts the user's ID from the Authorization header
     * @param authHeader The Authorization header value (e.g., "Bearer <token>")
     * @return The user's ID, or null if header is invalid or token is invalid
     */
    public String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.getUserId(token);
    }

    /**
     * Extracts the user's name from the Authorization header
     * @param authHeader The Authorization header value (e.g., "Bearer <token>")
     * @return The user's name, or null if header is invalid or token is invalid
     */
    public String extractUserName(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.getUserName(token);
    }

    /**
     * Checks if the provided Authorization header contains a valid JWT from an admin user
     * @param authHeader The Authorization header value (e.g., "Bearer <token>")
     * @return true if the token is valid and user ID is in the admin list, false otherwise
     */
    public boolean isAuthorizedAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.isServiceAdmin(token);
    }
}
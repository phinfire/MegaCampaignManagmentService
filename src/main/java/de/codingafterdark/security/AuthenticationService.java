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
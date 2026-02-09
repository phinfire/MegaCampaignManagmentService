package de.codingafterdark.security;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final Dotenv dotenv = Dotenv.load();
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private PublicKey cachedPublicKey;
    private long keyFetchTime;
    private static final long KEY_CACHE_DURATION = 3600000;

    public JwtTokenProvider() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        refreshPublicKey();
    }

    /**
     * Validates JWT token signature and returns the claims if valid
     * @param token The JWT token to validate
     * @return Claims if token is valid and signature is verified
     * @throws JwtException if token is invalid or signature verification fails
     */
    public Claims validateToken(String token) throws JwtException {
        try {
            PublicKey publicKey = getPublicKey();
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new JwtException("Invalid JWT signature: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            throw new JwtException("Expired JWT token: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new JwtException("Unsupported JWT token: " + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new JwtException("Invalid JWT token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT claims string is empty: " + e.getMessage());
        }
    }

    /**
     * Checks if the user ID from the JWT token is in the admin list configured in ADMIN_IDS env var
     * @param token The JWT token
     * @return true if token is valid and userId/discordId is in admin list, false otherwise
     */
    public boolean isServiceAdmin(String token) {
        try {
            Claims claims = validateToken(token);
            String userId = claims.get("userId", String.class);
            
            if (userId == null) {
                userId = claims.get("discordId", String.class);
            }
            
            if (userId == null) {
                return false;
            }
            
            String adminIdsEnv = dotenv.get("ADMIN_IDS", "");
            if (adminIdsEnv.isEmpty()) {
                return false;
            }
            
            String[] adminIds = adminIdsEnv.split(",");
            for (String adminId : adminIds) {
                if (adminId.trim().equals(userId)) {
                    return true;
                }
            }
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Gets the public key, fetching it if necessary or if cache expired
     */
    private PublicKey getPublicKey() throws JwtException {
        if (cachedPublicKey == null || isCacheExpired()) {
            refreshPublicKey();
        }
        return cachedPublicKey;
    }

    /**
     * Fetches the public key from the configured endpoint.
     * Expects JSON response with format: {"public_key": "-----BEGIN PUBLIC KEY-----..."}
     */
    private void refreshPublicKey() throws JwtException {
        try {
            String endpoint = dotenv.get("JWT_PUBLIC_KEY_ENDPOINT");
            if (endpoint == null || endpoint.isEmpty()) {
                throw new JwtException("JWT_PUBLIC_KEY_ENDPOINT not configured in .env file");
            }

            String jsonResponse = restTemplate.getForObject(endpoint, String.class);
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            String publicKeyPEM = jsonNode.get("public_key").asText();

            cachedPublicKey = parsePublicKey(publicKeyPEM);
            keyFetchTime = System.currentTimeMillis();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Failed to fetch and parse public key: " + e.getMessage());
        }
    }

    private boolean isCacheExpired() {
        return System.currentTimeMillis() - keyFetchTime > KEY_CACHE_DURATION;
    }

    /**
     * Extracts the public key from PEM format string
     */
    private PublicKey parsePublicKey(String publicKeyPEM) throws JwtException {
        try {
            String publicKeyContent = publicKeyPEM
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decodedKey = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new JwtException("Failed to parse public key: " + e.getMessage());
        }
    }
}


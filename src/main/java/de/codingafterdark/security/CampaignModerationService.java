package de.codingafterdark.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.codingafterdark.megacampaign.MegaCampaign;

@Service
public class CampaignModerationService {
    private final AuthenticationService authenticationService;

    @Autowired
    public CampaignModerationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Checks if the provided Authorization header contains a valid JWT from a moderator of the specified campaign
     * @param authHeader The Authorization header value (e.g., "Bearer <token>")
     * @param campaign The MegaCampaign to check moderator status for
     * @return true if the token is valid and the user ID is in the campaign's moderator list, false otherwise
     */
    public boolean isAuthorizedModerator(String authHeader, MegaCampaign campaign) {
        if (authHeader == null || !authHeader.startsWith("Bearer ") || campaign == null) {
            return false;
        }
        
        String userId = authenticationService.extractUserId(authHeader);
        if (userId == null) {
            return false;
        }
        
        List<Long> moderatorIds = campaign.getModeratorIds();
        if (moderatorIds == null || moderatorIds.isEmpty()) {
            return false;
        }
        
        try {
            Long userIdAsLong = Long.parseLong(userId);
            return moderatorIds.contains(userIdAsLong);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

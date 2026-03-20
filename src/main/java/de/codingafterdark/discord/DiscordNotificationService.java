package de.codingafterdark.discord;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class DiscordNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(DiscordNotificationService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${discord.notification.enabled:true}")
    private boolean enabled;

    @Value("${discord.api.endpoint}")
    private String apiEndpoint;

    @Autowired
    public DiscordNotificationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends a message to the configured Discord channel listing the signed up user
     * IDs
     * 
     * @param userIds List of user IDs to include in the message
     */
    public void notifyNewSignups(List<String> userIds) {
        if (!enabled) {
            logger.debug("Discord notifications are disabled");
            return;
        }

        if (userIds == null || userIds.isEmpty()) {
            logger.debug("No user IDs provided for Discord notification");
            return;
        }

        try {
            String message = formatSignupMessage(userIds);
            sendDiscordMessage(message);
        } catch (Exception e) {
            logger.error("Failed to send Discord notification", e);
            // Don't throw - we don't want signup failures if Discord notification fails
        }
    }

    /**
     * Sends a custom message to the configured Discord channel
     * 
     * @param message The message content to send
     */
    private void sendDiscordMessage(String message) throws Exception {
        if (apiEndpoint == null || apiEndpoint.isEmpty()) {
            logger.warn("Discord API endpoint not configured");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("content", message);

        try {
            restTemplate.postForObject(apiEndpoint, new org.springframework.http.HttpEntity<>(payload, headers),
                    Object.class);
            logger.info("Successfully sent Discord notification");
        } catch (RestClientException e) {
            logger.error("Error sending message to Discord: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Formats the signup message with the list of user IDs
     */
    private String formatSignupMessage(List<String> userIds) {
        String userList = String.join(", ", userIds);
        return String.format("📋 **Signed up users:** %s", userList);
    }
}

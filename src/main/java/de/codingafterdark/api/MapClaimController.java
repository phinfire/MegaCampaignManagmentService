package de.codingafterdark.api;

import de.codingafterdark.dto.MapClaimSessionHeader;
import de.codingafterdark.dto.MapClaimCountryData;
import de.codingafterdark.mapclaim.MapClaimSession;
import de.codingafterdark.mapclaim.MapClaimSessionRepository;
import de.codingafterdark.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mapclaims")
public class MapClaimController {

    private final MapClaimSessionRepository mapClaimSessionRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public MapClaimController(MapClaimSessionRepository mapClaimSessionRepository,
                            AuthenticationService authenticationService) {
        this.mapClaimSessionRepository = mapClaimSessionRepository;
        this.authenticationService = authenticationService;
    }

    /**
     * Gets all available map claim sessions for the authenticated user.
     * Returns sessions that are public or created by the user.
     * 
     * @param authHeader Authorization header containing the JWT token
     * @return ResponseEntity with list of available sessions
     */
    @GetMapping
    public ResponseEntity<List<MapClaimSessionHeader>> getSessions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = authenticationService.extractUserId(authHeader);
        List<MapClaimSession> sessions;
        if (userId == null) {
            sessions = mapClaimSessionRepository.findByIsPublicOrCreatorId(true, "");
        } else {
            sessions = mapClaimSessionRepository.findByIsPublicOrCreatorId(true, userId);
        }
        
        List<MapClaimSessionHeader> headers = sessions.stream()
                .map(session -> new MapClaimSessionHeader(
                        session.getId(),
                        session.getName(),
                        session.getIsPublic(),
                        session.getCreatorId()))
                .toList();
        return ResponseEntity.ok(headers);
    }

    /**
     * Gets a specific map claim session by ID.
     * Public sessions are accessible to anyone.
     * Private sessions are only accessible to the creator.
     * 
     * @param sessionId ID of the session to retrieve
     * @param authHeader Authorization header containing the JWT token
     * @return ResponseEntity with the session details
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSession(
            @PathVariable Long sessionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        MapClaimSession session = mapClaimSessionRepository.findById(sessionId)
                .orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        String userId = authenticationService.extractUserId(authHeader);
        
        // Check if session is public or if user is the creator
        if (!session.getIsPublic() && (userId == null || !session.getCreatorId().equals(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(session);
    }

    /**
     * Creates a new map claim session.
     * 
     * @param session MapClaimSession object with session data
     * @param authHeader Authorization header containing the JWT token
     * @return ResponseEntity with the ID of the created session
     */
    @PostMapping
    public ResponseEntity<?> createSession(
            @RequestBody MapClaimSession session,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = authenticationService.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        session.setCreatorId(userId);
        MapClaimSession saved = mapClaimSessionRepository.save(session);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(java.util.Map.of("id", saved.getId()));
    }

    /**
     * Deletes a map claim session.
     * Only the creator can delete their own session.
     * 
     * @param sessionId ID of the session to delete
     * @param authHeader Authorization header containing the JWT token
     * @return ResponseEntity with no content on success
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteSession(
            @PathVariable Long sessionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = authenticationService.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MapClaimSession session = mapClaimSessionRepository.findById(sessionId)
                .orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        if (!session.getCreatorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        mapClaimSessionRepository.deleteById(sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates properties of a map claim session.
     * Supports updating countries, isPublic, and name.
     * Only the creator can update their own session.
     * 
     * @param sessionId ID of the session to update
     * @param updates Map containing the properties to update
     * @param authHeader Authorization header containing the JWT token
     * @return ResponseEntity with no content on success
     */
    @PatchMapping("/{sessionId}")
    public ResponseEntity<?> updateSession(
            @PathVariable Long sessionId,
            @RequestBody java.util.Map<String, Object> updates,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = authenticationService.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        MapClaimSession session = mapClaimSessionRepository.findById(sessionId)
                .orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        if (!session.getCreatorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (updates.containsKey("countries")) {
            Object countriesObj = updates.get("countries");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, MapClaimCountryData> countriesMap = mapper.convertValue(countriesObj, 
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, MapClaimCountryData>>() {});
            session.setCountries(countriesMap);
        }
        if (updates.containsKey("isPublic")) {
            session.setIsPublic((Boolean) updates.get("isPublic"));
        }
        if (updates.containsKey("name")) {
            session.setName((String) updates.get("name"));
        }

        mapClaimSessionRepository.save(session);
        return ResponseEntity.noContent().build();
    }

    /**
     * Replaces the entire ownership map of a map claim session.
     * Only the creator can update ownership.
     * 
     * @param sessionId ID of the session to update
     * @param ownershipMap The new ownership map (key -> countryId)
     * @param authHeader Authorization header containing the JWT token
     * @return ResponseEntity with no content on success
     */
    @PutMapping("/{sessionId}/ownership")
    public ResponseEntity<?> replaceOwnerships(
            @PathVariable Long sessionId,
            @RequestBody java.util.Map<String, String> ownershipMap,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = authenticationService.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MapClaimSession session = mapClaimSessionRepository.findById(sessionId)
                .orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        if (!session.getCreatorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        session.setOwnership(ownershipMap);
        mapClaimSessionRepository.save(session);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds or updates a single ownership entry in a map claim session.
     * Only the creator can update ownership.
     * O(1) operation for individual entry updates.
     * 
     * @param sessionId ID of the session to update
     * @param key The ownership key
     * @param body Request body containing the countryId value
     * @param authHeader Authorization header containing the JWT token
     * @return ResponseEntity with no content on success
     */
    @PatchMapping("/{sessionId}/ownership/{key}")
    public ResponseEntity<?> updateOwnershipEntry(
            @PathVariable Long sessionId,
            @PathVariable String key,
            @RequestBody java.util.Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = authenticationService.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MapClaimSession session = mapClaimSessionRepository.findById(sessionId)
                .orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        if (!session.getCreatorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String countryId = body.get("countryId");
        if (countryId == null) {
            return ResponseEntity.badRequest().build();
        }

        if (session.getOwnership() == null) {
            session.setOwnership(new java.util.HashMap<>());
        }
        session.getOwnership().put(key, countryId);

        mapClaimSessionRepository.save(session);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a country from a map claim session.
     * Only the creator can delete countries.
     * Removes the country from countries and removes any ownership entries for this country.
     * 
     * @param sessionId ID of the session
     * @param countryId ID of the country to delete
     * @param authHeader Authorization header containing the JWT token
     * @return ResponseEntity with no content on success
     */
    @DeleteMapping("/{sessionId}/countries/{countryId}")
    public ResponseEntity<?> deleteCountry(
            @PathVariable Long sessionId,
            @PathVariable String countryId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = authenticationService.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        MapClaimSession session = mapClaimSessionRepository.findById(sessionId)
                .orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        if (!session.getCreatorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (session.getCountries() != null) {
            session.getCountries().remove(countryId);
        }
        if (session.getOwnership() != null) {
            session.getOwnership().entrySet().removeIf(entry -> entry.getValue().equals(countryId));
        }
        mapClaimSessionRepository.save(session);
        return ResponseEntity.noContent().build();
    }
}

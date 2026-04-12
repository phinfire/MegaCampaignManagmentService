package de.codingafterdark.mapclaim;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MapClaimSessionRepository extends JpaRepository<MapClaimSession, Long> {
    
    /**
     * Find all sessions that are either public or created by the given user.
     * @param isPublic whether the session is public
     * @param creatorId ID of the user
     * @return List of available sessions
     */
    List<MapClaimSession> findByIsPublicOrCreatorId(Boolean isPublic, String creatorId);
}

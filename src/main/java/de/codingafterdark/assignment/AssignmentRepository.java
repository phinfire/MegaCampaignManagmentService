package de.codingafterdark.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByMegaCampaignId(Long megaCampaignId);
    
    Optional<Assignment> findByMegaCampaignIdAndRegionKey(Long megaCampaignId, String regionKey);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM assignments WHERE mega_campaign_id = :campaignId", nativeQuery = true)
    void deleteByMegaCampaignId(@Param("campaignId") Long campaignId);
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO assignments (mega_campaign_id, user_id, region_key) VALUES (:campaignId, :userId, :regionKey) " +
           "ON CONFLICT (mega_campaign_id, user_id) DO UPDATE SET region_key = EXCLUDED.region_key", 
           nativeQuery = true)
    void upsertAssignment(@Param("campaignId") Long campaignId, @Param("userId") String userId, @Param("regionKey") String regionKey);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM assignments WHERE mega_campaign_id = :campaignId AND user_id = :userId", nativeQuery = true)
    void deleteByUserIdAndMegaCampaignId(@Param("userId") String userId, @Param("campaignId") Long campaignId);
}

package de.codingafterdark.signup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SignupRepository extends JpaRepository<Signup, Long> {
    
    @Query("SELECT s FROM Signup s WHERE s.megaCampaign.id = :campaignId ORDER BY s.submittedAt DESC")
    List<Signup> findByCampaignId(@Param("campaignId") Long campaignId);
    
    @Query("SELECT COUNT(DISTINCT s.userId) FROM Signup s WHERE s.megaCampaign.id = :campaignId AND SIZE(s.preferenceKeys) > 0 AND s.id IN (SELECT MAX(s2.id) FROM Signup s2 WHERE s2.megaCampaign.id = :campaignId GROUP BY s2.userId)")
    long countDistinctUsersByCampaignId(@Param("campaignId") Long campaignId);
    
    @Query("SELECT s FROM Signup s WHERE s.megaCampaign.id = :campaignId AND s.userId = :userId AND SIZE(s.preferenceKeys) > 0 AND s.id IN (SELECT MAX(s2.id) FROM Signup s2 WHERE s2.megaCampaign.id = :campaignId AND s2.userId = :userId)")
    Optional<Signup> findLatestSignupByCampaignIdAndUserId(@Param("campaignId") Long campaignId, @Param("userId") String userId);
    
    @Query("SELECT s FROM Signup s WHERE s.megaCampaign.id = :campaignId AND SIZE(s.preferenceKeys) > 0 AND s.id IN (SELECT MAX(s2.id) FROM Signup s2 WHERE s2.megaCampaign.id = :campaignId GROUP BY s2.userId) ORDER BY s.submittedAt DESC")
    List<Signup> findLatestSignupsPerUserByCampaignId(@Param("campaignId") Long campaignId);
}
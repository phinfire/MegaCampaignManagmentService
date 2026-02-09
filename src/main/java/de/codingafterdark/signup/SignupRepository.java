package de.codingafterdark.signup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SignupRepository extends JpaRepository<Signup, Long> {
    
    @Query("SELECT s FROM Signup s WHERE s.megaCampaign.id = :campaignId ORDER BY s.submittedAt DESC")
    List<Signup> findByCampaignId(@Param("campaignId") Long campaignId);
    
    @Query("SELECT COUNT(DISTINCT s.userId) FROM Signup s WHERE s.megaCampaign.id = :campaignId")
    long countDistinctUsersByCampaignId(@Param("campaignId") Long campaignId);
}
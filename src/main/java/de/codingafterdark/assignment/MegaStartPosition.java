package de.codingafterdark.assignment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "mega_start_positions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "mega_campaign_id"})
})
public class MegaStartPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "mega_campaign_id", nullable = false)
    private Long megaCampaignId;

    @Column(name = "start_key")
    private String startKey;

    @Column(name = "start_data", columnDefinition = "TEXT")
    private String startData;

    public MegaStartPosition(String userId, Long megaCampaignId, String startKey, String startData) {
        this.userId = userId;
        this.megaCampaignId = megaCampaignId;
        this.startKey = startKey;
        this.startData = startData;
    }
}

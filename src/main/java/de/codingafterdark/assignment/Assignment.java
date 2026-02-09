package de.codingafterdark.assignment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import de.codingafterdark.megacampaign.MegaCampaign;

@Data
@NoArgsConstructor
@Entity
@Table(name = "assignments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"mega_campaign_id", "user_id"}),
    @UniqueConstraint(columnNames = {"mega_campaign_id", "region_key"})
})
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mega_campaign_id", nullable = false)
    private MegaCampaign megaCampaign;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String regionKey;

    public Assignment(MegaCampaign megaCampaign, String userId, String regionKey) {
        this.megaCampaign = megaCampaign;
        this.userId = userId;
        this.regionKey = regionKey;
    }
}

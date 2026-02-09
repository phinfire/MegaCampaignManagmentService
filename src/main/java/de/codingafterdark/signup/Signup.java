package de.codingafterdark.signup;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import de.codingafterdark.megacampaign.MegaCampaign;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "signups", uniqueConstraints = @UniqueConstraint(columnNames = {"mega_campaign_id", "user_id", "submitted_at"}))
public class Signup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mega_campaign_id", nullable = false)
    private MegaCampaign megaCampaign;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Instant submittedAt;

    @Column(nullable = false)
    private String enteredBy;

    @ElementCollection
    @CollectionTable(name = "signup_preference_keys", joinColumns = @JoinColumn(name = "signup_id"))
    @Column(name = "preference_key")
    private List<String> preferenceKeys;

    public Signup(MegaCampaign megaCampaign, String userId, String enteredBy, List<String> preferenceKeys) {
        this.megaCampaign = megaCampaign;
        this.userId = userId;
        this.submittedAt = Instant.now();
        this.enteredBy = enteredBy;
        this.preferenceKeys = preferenceKeys != null ? preferenceKeys : new ArrayList<>();
    }
}
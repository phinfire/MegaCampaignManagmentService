package de.codingafterdark.megacampaign;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "mega_campaigns")
public class MegaCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private Boolean signupsOpen = false;
    
    @Column
    private Instant signupDeadlineDate;

    @Column
    private Instant pickDeadline;
    
    @Column
    private Instant firstSessionDate;

    @Column
    private Instant firstEu4SessionDate;
    
    @Column 
    private String ck3MapGeoJsonUrl;

    @Column 
    private String ck3RegionsConfigUrl;

    @Column
    private String nationsJsonUrl;

    @ElementCollection
    @CollectionTable(name = "mega_campaign_moderator_ids", joinColumns = @JoinColumn(name = "mega_campaign_id"))
    @Column(name = "moderator_id")
    private List<Long> moderatorIds;

    @ElementCollection
    @CollectionTable(name = "mega_campaign_ck3_lobbies", joinColumns = @JoinColumn(name = "mega_campaign_id"))
    @Column(name = "lobby_identifier")
    private List<String> ck3LobbiesIdentifiers;

    @ElementCollection
    @CollectionTable(name = "mega_campaign_eu4_lobbies", joinColumns = @JoinColumn(name = "mega_campaign_id"))
    @Column(name = "lobby_identifier")
    private List<String> eu4LobbiesIdentifiers;

    @ElementCollection
    @CollectionTable(name = "mega_campaign_vic3_lobbies", joinColumns = @JoinColumn(name = "mega_campaign_id"))
    @Column(name = "lobby_identifier")
    private List<String> vic3LobbyIdentifiers;

    @ElementCollection
    @CollectionTable(name = "mega_campaign_possible_keys", joinColumns = @JoinColumn(name = "mega_campaign_id"))
    @Column(name = "possible_key")
    private List<String> possibleKeys;

    public MegaCampaign(String name, Boolean signupsOpen,
            Instant signupDeadlineDate, Instant pickDeadline,
            Instant firstSessionDate, Instant firstEu4SessionDate) {
        this.name = name;
        this.signupsOpen = signupsOpen;
        this.signupDeadlineDate = signupDeadlineDate;
        this.pickDeadline = pickDeadline;
        this.firstSessionDate = firstSessionDate;
        this.firstEu4SessionDate = firstEu4SessionDate;
        this.ck3LobbiesIdentifiers = new ArrayList<>();
        this.eu4LobbiesIdentifiers = new ArrayList<>();
        this.vic3LobbyIdentifiers = new ArrayList<>();
        this.possibleKeys = new ArrayList<>();
        this.moderatorIds = new ArrayList<>();
    }
}
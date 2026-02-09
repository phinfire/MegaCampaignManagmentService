package de.codingafterdark.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MegaCampaignUpdate {
    private String name;
    private Boolean signupsOpen;
    private Instant signupDeadlineDate;
    private Instant pickDeadline;
    private Instant firstSessionDate;
    private Instant firstEu4SessionDate;
    private List<Long> moderatorIds;
    private List<String> ck3LobbiesIdentifiers;
    private List<String> eu4LobbiesIdentifiers;
    private List<String> vic3LobbyIdentifiers;
    private List<String> possibleKeys;
}

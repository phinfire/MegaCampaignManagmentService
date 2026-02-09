package de.codingafterdark.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import de.codingafterdark.megacampaign.MegaCampaign;
import de.codingafterdark.megacampaign.MegaCampaignRepository;
import de.codingafterdark.security.AuthenticationService;
import de.codingafterdark.dto.MegaCampaignUpdate;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@RestController
public class MegaCampaignController {
    private final MegaCampaignRepository campaignRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public MegaCampaignController(MegaCampaignRepository campaignRepository,
            AuthenticationService authenticationService) {
        this.campaignRepository = campaignRepository;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/campaigns")
    public List<MegaCampaign> getCampaigns() {
        return campaignRepository.findAll();
    }

    @PostMapping("/campaigns")
    public ResponseEntity<MegaCampaign> createCampaign(@RequestParam String name,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        MegaCampaign campaign = new MegaCampaign(name, false, null, null, null, null);
        MegaCampaign saved = campaignRepository.save(campaign);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PatchMapping("/campaigns/{id}")
    public ResponseEntity<MegaCampaign> updateCampaign(
            @PathVariable Long id,
            @RequestBody MegaCampaignUpdate updates,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<MegaCampaign> existing = campaignRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        System.out.println("Received update for campaign " + id + ": " + updates);

        MegaCampaign campaign = existing.get();
        
        if (updates.getName() != null) {
            campaign.setName(updates.getName());
        }
        if (updates.getSignupsOpen() != null) {
            campaign.setSignupsOpen(updates.getSignupsOpen());
        }
        if (updates.getSignupDeadlineDate() != null) {
            campaign.setSignupDeadlineDate(updates.getSignupDeadlineDate());
        }
        if (updates.getPickDeadline() != null) {
            campaign.setPickDeadline(updates.getPickDeadline());
        }
        if (updates.getFirstSessionDate() != null) {
            campaign.setFirstSessionDate(updates.getFirstSessionDate());
        }
        if (updates.getFirstEu4SessionDate() != null) {
            campaign.setFirstEu4SessionDate(updates.getFirstEu4SessionDate());
        }
        if (updates.getModeratorIds() != null) {
            campaign.setModeratorIds(updates.getModeratorIds());
        }
        if (updates.getCk3LobbiesIdentifiers() != null) {
            campaign.setCk3LobbiesIdentifiers(updates.getCk3LobbiesIdentifiers());
        }
        if (updates.getEu4LobbiesIdentifiers() != null) {
            campaign.setEu4LobbiesIdentifiers(updates.getEu4LobbiesIdentifiers());
        }
        if (updates.getVic3LobbyIdentifiers() != null) {
            campaign.setVic3LobbyIdentifiers(updates.getVic3LobbyIdentifiers());
        }
        if (updates.getPossibleKeys() != null) {
            campaign.setPossibleKeys(updates.getPossibleKeys());
        }
        
        MegaCampaign saved = campaignRepository.save(campaign);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!campaignRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        campaignRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
package de.codingafterdark.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import de.codingafterdark.megacampaign.MegaCampaign;
import de.codingafterdark.megacampaign.MegaCampaignRepository;
import de.codingafterdark.signup.Signup;
import de.codingafterdark.signup.SignupRepository;
import de.codingafterdark.security.AuthenticationService;
import de.codingafterdark.dto.SignupRequest;
import de.codingafterdark.dto.SignupLatestView;
import de.codingafterdark.dto.SignupHistoryView;

@RestController
public class SignupController {
    private final SignupRepository signupRepository;
    private final MegaCampaignRepository megaCampaignRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public SignupController(SignupRepository signupRepository,
            MegaCampaignRepository megaCampaignRepository,
            AuthenticationService authenticationService) {
        this.signupRepository = signupRepository;
        this.megaCampaignRepository = megaCampaignRepository;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/campaigns/{campaignId}/signups")
    public ResponseEntity<Signup> createSignup(
            @PathVariable Long campaignId,
            @RequestBody SignupRequest signupRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Optional<MegaCampaign> campaign = megaCampaignRepository.findById(campaignId);
        if (campaign.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!campaign.get().getSignupsOpen()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Signup signup = new Signup(
            campaign.get(),
            signupRequest.getUserId(),
            signupRequest.getEnteredBy(),
            signupRequest.getPreferenceKeys()
        );
        Signup saved = signupRepository.save(signup);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/campaigns/{campaignId}/signups/count")
    public ResponseEntity<Long> getSignupCount(@PathVariable Long campaignId) {
        Optional<MegaCampaign> campaign = megaCampaignRepository.findById(campaignId);
        if (campaign.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        long count = signupRepository.countDistinctUsersByCampaignId(campaignId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/campaigns/{campaignId}/signups/latest")
    public ResponseEntity<List<SignupLatestView>> getLatestSignups(@PathVariable Long campaignId) {
        Optional<MegaCampaign> campaign = megaCampaignRepository.findById(campaignId);
        if (campaign.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Signup> allSignups = signupRepository.findByCampaignId(campaignId);
        List<SignupLatestView> latest = allSignups.stream()
            .collect(Collectors.toMap(
                Signup::getUserId,
                s -> s,
                (existing, newer) -> newer.getSubmittedAt().isAfter(existing.getSubmittedAt()) ? newer : existing
            ))
            .values()
            .stream()
            .map(SignupLatestView::fromSignup)
            .collect(Collectors.toList());

        return ResponseEntity.ok(latest);
    }

    @GetMapping("/campaigns/{campaignId}/signups")
    public ResponseEntity<List<SignupHistoryView>> getSignupHistory(
            @PathVariable Long campaignId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<MegaCampaign> campaign = megaCampaignRepository.findById(campaignId);
        if (campaign.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Signup> signups = signupRepository.findByCampaignId(campaignId);
        List<SignupHistoryView> history = signups.stream()
            .map(SignupHistoryView::fromSignup)
            .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }
}


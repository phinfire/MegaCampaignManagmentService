package de.codingafterdark.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import de.codingafterdark.discord.DiscordNotificationService;
import de.codingafterdark.dto.SignupLatestView;
import de.codingafterdark.dto.SignupRequest;
import de.codingafterdark.exception.BadSignupException;
import de.codingafterdark.exception.NoSuchCampaignException;
import de.codingafterdark.exception.NoSignupForUserException;
import de.codingafterdark.exception.NoUserInJWTException;
import de.codingafterdark.exception.NotAnAdminException;
import de.codingafterdark.megacampaign.MegaCampaign;
import de.codingafterdark.megacampaign.MegaCampaignRepository;
import de.codingafterdark.security.AuthenticationService;
import de.codingafterdark.security.CampaignModerationService;
import de.codingafterdark.signup.Signup;
import de.codingafterdark.signup.SignupRepository;

@RestController
public class SignupController {
    private final SignupRepository signupRepository;
    private final MegaCampaignRepository megaCampaignRepository;
    private final AuthenticationService authenticationService;
    private final CampaignModerationService campaignModerationService;
    private final DiscordNotificationService discordNotificationService;

    @Autowired
    public SignupController(SignupRepository signupRepository,
            MegaCampaignRepository megaCampaignRepository,
            AuthenticationService authenticationService,
            CampaignModerationService campaignModerationService,
            DiscordNotificationService discordNotificationService) {
        this.signupRepository = signupRepository;
        this.megaCampaignRepository = megaCampaignRepository;
        this.authenticationService = authenticationService;
        this.campaignModerationService = campaignModerationService;
        this.discordNotificationService = discordNotificationService;
    }

    /**
     * Gets the count of unique signup users for a campaign. No authentication
     * required.
     * 
     * @param campaignId ID of the campaign
     * @return ResponseEntity with the count of unique users who signed up
     * @throws NoSuchCampaignException if the specified campaign does not exist
     */
    @GetMapping("/campaigns/{campaignId}/signups/count")
    public ResponseEntity<Long> getSignupCount(
            @PathVariable Long campaignId) {
        megaCampaignRepository.findById(campaignId).orElseThrow(() -> new NoSuchCampaignException(campaignId));
        long count = signupRepository.countDistinctUsersByCampaignId(campaignId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/campaigns/{campaignId}/signup/{userId}")
    public ResponseEntity<Signup> getSignup(
            @PathVariable Long campaignId,
            @PathVariable String userId) {
        megaCampaignRepository.findById(campaignId).orElseThrow(() -> new NoSuchCampaignException(campaignId));
        Signup signup = signupRepository.findLatestSignupByCampaignIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new NoSignupForUserException(campaignId, userId));
        return ResponseEntity.ok(signup);
    }

    /**
     * Deletes a signup
     * Admins can delete any signup, users can only delete their own.
     * 
     * @param campaignId ID of the campaign
     * @param userId     ID of the user whose signup should be deleted
     * @param authHeader Authorization header containing the JWT token of the
     *                   requester
     * @return ResponseEntity with no content on success
     * @throws NotAnAdminException      if the requester is neither an admin nor the
     *                                  user being deleted
     * @throws NoSuchCampaignException  if the specified campaign does not exist
     * @throws NoSignupForUserException if no signup exists for the user in this
     *                                  campaign
     */
    @DeleteMapping("/campaigns/{campaignId}/signup/{userId}")
    public ResponseEntity<?> deleteSignup(
            @PathVariable Long campaignId,
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String currentUserId = authenticationService.extractUserId(authHeader);
        boolean isAdmin = authenticationService.isAuthorizedAdmin(authHeader);
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(userId))) {
            throw new NotAnAdminException();
        }
        megaCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchCampaignException(campaignId));
        signupRepository.findLatestSignupByCampaignIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new NoSignupForUserException(campaignId, userId));
        if (currentUserId == null) {
            throw new NoUserInJWTException();
        }
        MegaCampaign campaign = megaCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchCampaignException(campaignId));
        Signup cancelSignup = new Signup(
                campaign,
                userId,
                currentUserId,
                new java.util.ArrayList<>());
        signupRepository.save(cancelSignup);
        return ResponseEntity.noContent().build();
    }

    /*
     * Creates a new signup for the specified campaign.
     * 
     * @param campaignId ID of the campaign for which to create a signup
     * 
     * @param signupRequest Request body containing the signup details
     * 
     * @param authHeader Authorization header containing the JWT token of the
     * requester
     * 
     * @return ResponseEntity with the created Signup object
     * 
     * @throws NoUserInJWTException if the requester is not authenticated
     * 
     * @throws NoSuchCampaignException if the specified campaign does not exist
     * 
     * @throws BadSignupException if the signup request is invalid
     */
    @PostMapping("/campaigns/{campaignId}/signups")
    public ResponseEntity<Signup> createSignup(
            @PathVariable Long campaignId,
            @RequestBody SignupRequest signupRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String enteredBy = authenticationService.extractUserId(authHeader);
        if (enteredBy == null) {
            throw new NoUserInJWTException();
        }
        MegaCampaign campaign = megaCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchCampaignException(campaignId));

        if (!campaign.getSignupsOpen()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        if (signupRequest.getUserId() == null || signupRequest.getUserId().trim().isEmpty()) {
            throw new BadSignupException("Signup request is missing a valid userId");
        }
        if (signupRequest.getPreferenceKeys() == null) {
            throw new BadSignupException("Signup request is missing preference keys");
        }
        Signup signup = new Signup(
                campaign,
                signupRequest.getUserId(),
                enteredBy,
                signupRequest.getPreferenceKeys());
        Signup saved = signupRepository.save(signup);
        
        List<Signup> latestSignups = signupRepository.findLatestSignupsPerUserByCampaignId(campaignId);
        List<String> userIds = latestSignups.stream()
                .map(Signup::getUserId)
                .sorted()
                .collect(Collectors.toList());
        discordNotificationService.notifyNewSignups(userIds);
        
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/campaigns/{campaignId}/signups")
    public ResponseEntity<List<SignupLatestView>> getLatestSignups(
            @PathVariable Long campaignId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            throw new NotAnAdminException();
        }
        megaCampaignRepository.findById(campaignId).orElseThrow(() -> new NoSuchCampaignException(campaignId));
        List<Signup> latest = signupRepository.findLatestSignupsPerUserByCampaignId(campaignId);
        List<SignupLatestView> latestViews = latest.stream()
                .map(SignupLatestView::fromSignup)
                .collect(Collectors.toList());
        return ResponseEntity.ok(latestViews);
    }

    /**
     * Gets the latest signup for a specific user. Admins can view any user's
     * signup,
     * users can only view their own.
     * 
     * @param campaignId ID of the campaign
     * @param userId     ID of the user whose signup should be retrieved
     * @param authHeader Authorization header containing the JWT token of the
     *                   requester
     * @return ResponseEntity with the latest SignupLatestView for the user
     * @throws NotAnAdminException      if the requester is neither an admin nor the
     *                                  user being queried
     * @throws NoSuchCampaignException  if the specified campaign does not exist
     * @throws NoSignupForUserException if no signup exists for the user in this
     *                                  campaign
     */
    @GetMapping("/campaigns/{campaignId}/signups/{userId}")
    public ResponseEntity<SignupLatestView> getLatestSignupForUser(
            @PathVariable Long campaignId,
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String currentUserId = authenticationService.extractUserId(authHeader);
        boolean isAdmin = authenticationService.isAuthorizedAdmin(authHeader);
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(userId))) {
            throw new NotAnAdminException();
        }
        megaCampaignRepository.findById(campaignId).orElseThrow(() -> new NoSuchCampaignException(campaignId));
        return ResponseEntity.ok(signupRepository.findLatestSignupByCampaignIdAndUserId(campaignId, userId)
                .map(SignupLatestView::fromSignup)
                .orElseGet(() -> new SignupLatestView(userId, new ArrayList<>())));
    }

    /**
     * Creates a new signup for a specific user. Admins can create signups for any
     * user, users can only create for themselves.
     * 
     * @param campaignId    ID of the campaign for which to create a signup
     * @param userId        ID of the user for whom to create a signup
     * @param signupRequest Request body containing the signup details
     * @param authHeader    Authorization header containing the JWT token of the
     *                      requester
     * @return ResponseEntity with the created Signup object
     * @throws NotAnAdminException     if the requester is neither an admin nor the
     *                                 user being created for
     * @throws NoSuchCampaignException if the specified campaign does not exist
     * @throws BadSignupException      if the signup request is invalid
     */
    @PostMapping("/campaigns/{campaignId}/signups/{userId}")
    public ResponseEntity<Signup> createSignupForUser(
            @PathVariable Long campaignId,
            @PathVariable String userId,
            @RequestBody SignupRequest signupRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String currentUserId = authenticationService.extractUserId(authHeader);
        boolean isAdmin = authenticationService.isAuthorizedAdmin(authHeader);
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(userId))) {
            throw new NotAnAdminException();
        }
        if (currentUserId == null) {
            throw new NoUserInJWTException();
        }
        MegaCampaign campaign = megaCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchCampaignException(campaignId));

        if (!campaign.getSignupsOpen()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadSignupException("Signup request is missing a valid userId");
        }
        if (signupRequest.getPreferenceKeys() == null) {
            throw new BadSignupException("Signup request is missing preference keys");
        }
        Signup signup = new Signup(
                campaign,
                userId,
                currentUserId,
                signupRequest.getPreferenceKeys());
        Signup saved = signupRepository.save(signup);
        
        // Notify Discord of new signup
        List<Signup> latestSignups = signupRepository.findLatestSignupsPerUserByCampaignId(campaignId);
        List<String> userIds = latestSignups.stream()
                .map(Signup::getUserId)
                .sorted()
                .collect(Collectors.toList());
        discordNotificationService.notifyNewSignups(userIds);
        
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Gets an ordered list of user IDs who have signed up for a campaign.
     * Only moderators of the campaign can access this endpoint.
     * 
     * @param campaignId ID of the campaign
     * @param authHeader Authorization header containing the JWT token of the requester
     * @return ResponseEntity with an ordered list of signed up user IDs
     * @throws NoSuchCampaignException if the specified campaign does not exist
     * @throws NotAnAdminException if the requester is not a moderator of the campaign
     */
    @GetMapping("/campaigns/{campaignId}/signups/moderator/users")
    public ResponseEntity<List<String>> getSignedUpUserIds(
            @PathVariable Long campaignId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        MegaCampaign campaign = megaCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchCampaignException(campaignId));
        
        if (!campaignModerationService.isAuthorizedModerator(authHeader, campaign)) {
            throw new NotAnAdminException();
        }
        
        List<Signup> latestSignups = signupRepository.findLatestSignupsPerUserByCampaignId(campaignId);
        List<String> userIds = latestSignups.stream()
                .map(Signup::getUserId)
                .sorted()
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userIds);
    }
}
package de.codingafterdark.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import de.codingafterdark.assignment.Assignment;
import de.codingafterdark.assignment.AssignmentRepository;
import de.codingafterdark.assignment.MegaStartPosition;
import de.codingafterdark.assignment.MegaStartPositionRepository;
import de.codingafterdark.dto.AssignmentRequest;
import de.codingafterdark.dto.AssignmentView;
import de.codingafterdark.dto.MegaStartPositionRequest;
import de.codingafterdark.dto.MegaStartPositionView;
import de.codingafterdark.megacampaign.MegaCampaignRepository;
import de.codingafterdark.security.AuthenticationService;

@RestController
public class AssignmentController {
    private final AssignmentRepository assignmentRepository;
    private final MegaCampaignRepository megaCampaignRepository;
    private final MegaStartPositionRepository megaStartPositionRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public AssignmentController(AssignmentRepository assignmentRepository,
            MegaCampaignRepository megaCampaignRepository,
            MegaStartPositionRepository megaStartPositionRepository,
            AuthenticationService authenticationService) {
        this.assignmentRepository = assignmentRepository;
        this.megaCampaignRepository = megaCampaignRepository;
        this.megaStartPositionRepository = megaStartPositionRepository;
        this.authenticationService = authenticationService;
    }

    @PutMapping("/campaigns/{campaignId}/assignments")
    public ResponseEntity<?> updateAssignments(
            @PathVariable Long campaignId,
            @RequestBody AssignmentRequest assignmentRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        megaCampaignRepository.findById(campaignId)
            .orElseThrow();

        var userIds = assignmentRequest.getAssignments().stream()
            .map(a -> a.getUserId())
            .collect(Collectors.toList());
        if (userIds.size() != userIds.stream().distinct().count()) {
            return ResponseEntity.badRequest().body("Assignment request contains duplicate users");
        }

        var regionKeys = assignmentRequest.getAssignments().stream()
            .map(a -> a.getRegionKey())
            .collect(Collectors.toList());
        if (regionKeys.size() != regionKeys.stream().distinct().count()) {
            return ResponseEntity.badRequest().body("Assignment request contains duplicate regions");
        }

        assignmentRepository.deleteByMegaCampaignId(campaignId);
        for (var entry : assignmentRequest.getAssignments()) {
            assignmentRepository.upsertAssignment(campaignId, entry.getUserId(), entry.getRegionKey());
        }

        List<Assignment> assignments = assignmentRepository.findByMegaCampaignId(campaignId);
        List<AssignmentView> result = assignments.stream()
            .map(AssignmentView::fromAssignment)
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/campaigns/{campaignId}/assignments/{userId}/{regionKey}")
    public ResponseEntity<?> setAssignment(
            @PathVariable Long campaignId,
            @PathVariable String userId,
            @PathVariable String regionKey,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        megaCampaignRepository.findById(campaignId).orElseThrow();

        var existingAssignment = assignmentRepository.findByMegaCampaignIdAndRegionKey(campaignId, regionKey);
        if (existingAssignment.isPresent() && !existingAssignment.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Region already assigned to another user");
        }

        assignmentRepository.upsertAssignment(campaignId, userId, regionKey);
        var assignment = assignmentRepository.findByMegaCampaignIdAndRegionKey(campaignId, regionKey).orElseThrow();
        return ResponseEntity.ok(AssignmentView.fromAssignment(assignment));
    }

    @GetMapping("/campaigns/{campaignId}/assignments")
    public ResponseEntity<List<AssignmentView>> getAssignments(
            @PathVariable Long campaignId) {
        megaCampaignRepository.findById(campaignId)
            .orElseThrow();
        List<Assignment> assignments = assignmentRepository.findByMegaCampaignId(campaignId);
        List<AssignmentView> result = assignments.stream()
            .map(AssignmentView::fromAssignment)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/campaigns/{campaignId}/start-positions/{userId}")
    public ResponseEntity<MegaStartPositionView> setStartPosition(
            @PathVariable Long campaignId,
            @PathVariable String userId,
            @RequestBody MegaStartPositionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String authenticatedUserId = authenticationService.extractUserId(authHeader);
        if (authenticatedUserId == null || !authenticatedUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        megaCampaignRepository.findById(campaignId)
            .orElseThrow();

        MegaStartPosition position = megaStartPositionRepository.findByUserIdAndMegaCampaignId(userId, campaignId)
            .orElse(new MegaStartPosition());
        
        position.setUserId(userId);
        position.setMegaCampaignId(campaignId);
        position.setStartKey(request.getStartKey());
        position.setStartData(request.getStartData());

        MegaStartPosition saved = megaStartPositionRepository.save(position);
        return ResponseEntity.ok(MegaStartPositionView.fromMegaStartPosition(saved));
    }

    @GetMapping("/campaigns/{campaignId}/start-positions")
    public ResponseEntity<List<MegaStartPositionView>> getAllStartPositions(
            @PathVariable Long campaignId) {

        megaCampaignRepository.findById(campaignId)
            .orElseThrow();

        List<MegaStartPosition> positions = megaStartPositionRepository.findByMegaCampaignId(campaignId);
        List<MegaStartPositionView> result = positions.stream()
            .map(MegaStartPositionView::fromMegaStartPosition)
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/campaigns/{campaignId}/start-positions/{userId}")
    public ResponseEntity<MegaStartPositionView> getStartPosition(
            @PathVariable Long campaignId,
            @PathVariable String userId) {

        megaCampaignRepository.findById(campaignId)
            .orElseThrow();

        MegaStartPosition position = megaStartPositionRepository.findByUserIdAndMegaCampaignId(userId, campaignId)
            .orElseThrow();

        return ResponseEntity.ok(MegaStartPositionView.fromMegaStartPosition(position));
    }

    @DeleteMapping("/campaigns/{campaignId}/assignments/{userId}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long campaignId,
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        megaCampaignRepository.findById(campaignId)
            .orElseThrow();

        assignmentRepository.deleteByUserIdAndMegaCampaignId(userId, campaignId);
        return ResponseEntity.noContent().build();
    }
}
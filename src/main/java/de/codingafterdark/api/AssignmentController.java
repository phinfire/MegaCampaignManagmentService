package de.codingafterdark.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import de.codingafterdark.assignment.Assignment;
import de.codingafterdark.assignment.AssignmentRepository;
import de.codingafterdark.megacampaign.MegaCampaign;
import de.codingafterdark.megacampaign.MegaCampaignRepository;
import de.codingafterdark.security.AuthenticationService;
import de.codingafterdark.dto.AssignmentRequest;
import de.codingafterdark.dto.AssignmentView;

@RestController
public class AssignmentController {
    private final AssignmentRepository assignmentRepository;
    private final MegaCampaignRepository megaCampaignRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public AssignmentController(AssignmentRepository assignmentRepository,
            MegaCampaignRepository megaCampaignRepository,
            AuthenticationService authenticationService) {
        this.assignmentRepository = assignmentRepository;
        this.megaCampaignRepository = megaCampaignRepository;
        this.authenticationService = authenticationService;
    }

    @PutMapping("/campaigns/{campaignId}/assignments")
    public ResponseEntity<List<AssignmentView>> updateAssignments(
            @PathVariable Long campaignId,
            @RequestBody AssignmentRequest assignmentRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<MegaCampaign> campaign = megaCampaignRepository.findById(campaignId);
        if (campaign.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        assignmentRepository.deleteByMegaCampaignId(campaignId);

        List<Assignment> assignments = assignmentRequest.getAssignments().stream()
            .map(entry -> new Assignment(campaign.get(), entry.getUserId(), entry.getRegionKey()))
            .collect(Collectors.toList());

        List<Assignment> saved = assignmentRepository.saveAll(assignments);
        List<AssignmentView> result = saved.stream()
            .map(AssignmentView::fromAssignment)
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/campaigns/{campaignId}/assignments")
    public ResponseEntity<List<AssignmentView>> getAssignments(
            @PathVariable Long campaignId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!authenticationService.isAuthorizedAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<MegaCampaign> campaign = megaCampaignRepository.findById(campaignId);
        if (campaign.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Assignment> assignments = assignmentRepository.findByMegaCampaignId(campaignId);
        List<AssignmentView> result = assignments.stream()
            .map(AssignmentView::fromAssignment)
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}

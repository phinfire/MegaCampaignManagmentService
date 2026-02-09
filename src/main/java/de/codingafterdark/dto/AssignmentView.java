package de.codingafterdark.dto;

import de.codingafterdark.assignment.Assignment;

public class AssignmentView {
    private Long id;
    private String userId;
    private String regionKey;

    public AssignmentView(Long id, String userId, String regionKey) {
        this.id = id;
        this.userId = userId;
        this.regionKey = regionKey;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRegionKey() { return regionKey; }
    public void setRegionKey(String regionKey) { this.regionKey = regionKey; }

    public static AssignmentView fromAssignment(Assignment assignment) {
        return new AssignmentView(
            assignment.getId(),
            assignment.getUserId(),
            assignment.getRegionKey()
        );
    }
}

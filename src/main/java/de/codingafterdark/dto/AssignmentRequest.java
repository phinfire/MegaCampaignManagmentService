package de.codingafterdark.dto;

import java.util.List;

public class AssignmentRequest {
    private List<AssignmentEntry> assignments;

    public List<AssignmentEntry> getAssignments() { return assignments; }
    public void setAssignments(List<AssignmentEntry> assignments) { this.assignments = assignments; }

    public static class AssignmentEntry {
        private String userId;
        private String regionKey;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getRegionKey() { return regionKey; }
        public void setRegionKey(String regionKey) { this.regionKey = regionKey; }
    }
}

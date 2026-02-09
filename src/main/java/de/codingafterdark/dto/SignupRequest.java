package de.codingafterdark.dto;

import java.util.List;

public class SignupRequest {
    private String userId;
    private String enteredBy;
    private List<String> preferenceKeys;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEnteredBy() { return enteredBy; }
    public void setEnteredBy(String enteredBy) { this.enteredBy = enteredBy; }

    public List<String> getPreferenceKeys() { return preferenceKeys; }
    public void setPreferenceKeys(List<String> preferenceKeys) { this.preferenceKeys = preferenceKeys; }
}

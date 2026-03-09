package de.codingafterdark.dto;

import java.util.List;

public class SignupRequest {
    private String userId;
    private List<String> preferenceKeys;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getPreferenceKeys() { return preferenceKeys; }
    public void setPreferenceKeys(List<String> preferenceKeys) { this.preferenceKeys = preferenceKeys; }
}

package de.codingafterdark.dto;

import de.codingafterdark.signup.Signup;
import java.util.List;

public class SignupLatestView {
    private String userId;
    private List<String> preferenceKeys;

    public SignupLatestView(String userId, List<String> preferenceKeys) {
        this.userId = userId;
        this.preferenceKeys = preferenceKeys;
    }

    public static SignupLatestView fromSignup(Signup signup) {
        return new SignupLatestView(signup.getUserId(), signup.getPreferenceKeys());
    }

    public String getUserId() { return userId; }
    public List<String> getPreferenceKeys() { return preferenceKeys; }
}

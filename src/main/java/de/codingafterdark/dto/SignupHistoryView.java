package de.codingafterdark.dto;

import de.codingafterdark.signup.Signup;
import java.util.List;

public class SignupHistoryView {
    private String userId;
    private List<String> preferenceKeys;
    private String submittedAt;
    private String enteredBy;

    public SignupHistoryView(String userId, List<String> preferenceKeys, String submittedAt, String enteredBy) {
        this.userId = userId;
        this.preferenceKeys = preferenceKeys;
        this.submittedAt = submittedAt;
        this.enteredBy = enteredBy;
    }

    public static SignupHistoryView fromSignup(Signup signup) {
        return new SignupHistoryView(
            signup.getUserId(),
            signup.getPreferenceKeys(),
            signup.getSubmittedAt().toString(),
            signup.getEnteredBy()
        );
    }

    public String getUserId() { return userId; }
    public List<String> getPreferenceKeys() { return preferenceKeys; }
    public String getSubmittedAt() { return submittedAt; }
    public String getEnteredBy() { return enteredBy; }
}

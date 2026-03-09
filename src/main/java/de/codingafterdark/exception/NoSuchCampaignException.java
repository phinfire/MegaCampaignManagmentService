package de.codingafterdark.exception;

public class NoSuchCampaignException extends RuntimeException {
    public NoSuchCampaignException(Long campaignId) {
        super("Campaign not found for id: " + campaignId);
    }
}
